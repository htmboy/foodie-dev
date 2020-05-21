package com.imooc.controller.center;

import com.imooc.controller.BaseController;
import com.imooc.pojo.Users;
import com.imooc.pojo.bo.center.CenterUserBO;
import com.imooc.pojo.vo.UsersVO;
import com.imooc.resource.FileUpload;
import com.imooc.service.center.CenterUserService;
import com.imooc.utils.CookieUtils;
import com.imooc.utils.DateUtil;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "用户信息接口", tags = {"用户信息相关接口"})
@RequestMapping("userInfo")
@RestController
public class CenterUserController extends BaseController {

    @Autowired
    private CenterUserService centerUserService;

    @Autowired
    private FileUpload fileUpload;

    @ApiOperation(value = "用户头像修改", notes = "用户头像修改", httpMethod = "POST")
    @PostMapping("uploadFace")
    public IMOOCJSONResult uploadFace(
            @ApiParam(name = "userId", value = "用户id", required = true)
            @RequestParam String userId,
            @ApiParam(name = "file", value = "用户头像", required = true)
            MultipartFile file,
            HttpServletRequest request, HttpServletResponse response) {

        // 定义头像保存的地址
        // String fileSpace = IMAGE_USER_FACE_LOCALTION;
        String fileSpace = fileUpload.getImageUserFaceLocation();

        // 在路径上为每一个用户增加一个userId，用于区分不同用户上传
        String uploadPathPrefix = File.separator + userId;

        //开始文件上传
        if(file != null) {

            FileOutputStream fileOutputStream = null;

            // 获得文件上传的文件名称
            String fileName = file.getOriginalFilename();

            // 虽然大多情况下 filename 是不可能为空
            if(StringUtils.isNotBlank(fileName)){

                // 文件重命名 imooc-face.png -> ["imooc-face","png"]
                String fileNameArr[] = fileName.split("\\.");

                // 获取文件的后缀名
                String suffix = fileNameArr[fileNameArr.length - 1];

                // 重点：这里一定要判断上传上来的后缀名
                if(!suffix.equalsIgnoreCase("png") &&
                        !suffix.equalsIgnoreCase("jpg") &&
                        !suffix.equalsIgnoreCase("jpeg")){
                    return IMOOCJSONResult.errorMsg("图片格式不争正确");
                }

                // face-{userId}.png
                // 文件名称重组 覆盖式上传
                String newFileName = "face-" + userId + "." + suffix;

                // 上传的头像最终保存的位置
                String finalFacePath = fileSpace + uploadPathPrefix + File.separator + newFileName;

                // 用于提供给web服务器访问的地址
                uploadPathPrefix += ("/" + newFileName);

                File outFile = new File(finalFacePath);
                if(outFile.getParentFile() != null) {
                    outFile.getParentFile().mkdirs();
                }

                // 文件输出保存到目录
                try {
                    fileOutputStream = new FileOutputStream(outFile);
                    InputStream inputStream = file.getInputStream();
                    IOUtils.copy(inputStream, fileOutputStream);
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.flush();
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }

        }else{
            return IMOOCJSONResult.errorMsg("文件不能为空");
        }

        // 获取图片服务地址
        String imageServerUrl = fileUpload.getImageServerUrl();

        // 由于浏览器存在缓存，头像是不能在前端实时更新，所以要加上时间戳，来保证图片可以及时刷新
        String finalUserFaceUrl = imageServerUrl + uploadPathPrefix
                + "?t=" + DateUtil.getCurrentDateString(DateUtil.DATE_PATTERN);

        // 更新用户头像到数据库
        Users userResult = centerUserService.updateUserFace(userId, finalUserFaceUrl);

        // 增加令牌token，要整合进行redis，分布式会话
        UsersVO usersVO = conventUserVO(userResult);

        // 将无用属性设置为null
        // userResult = setNullProperty(userResult);

        // 重置 cookie
        CookieUtils.setCookie(request, response, "user", JsonUtils.objectToJson(usersVO),true);


        return IMOOCJSONResult.ok();
    }

    @ApiOperation(value = "修改用户信息", notes = "修改用户信息", httpMethod = "POST")
    @PostMapping("update")
    public IMOOCJSONResult update(
            @ApiParam(name = "userId", value = "用户id", required = true)
            @RequestParam String userId,
            @RequestBody @Valid CenterUserBO centerUserBO, // @Valid 表示此实体层要做验证
            BindingResult result, // @Valid 验证出错的信息将会放在 BindingResult 类里面
            HttpServletRequest request,
            HttpServletResponse response) {

        // 判断BindingResult 是否保存错误的验证信息，如果有，则直接return
        if (result.hasErrors()) {
            Map<String, String> errorMap = getErrors(result);
            return IMOOCJSONResult.errorMap(errorMap);
        }

        // 更新数据库
        Users userResult = centerUserService.updateUserInfo(userId, centerUserBO);

        // 增加令牌token，要整合进行redis，分布式会话
        UsersVO usersVO = conventUserVO(userResult);

        // 将无用属性设置为null
        // userResult = setNullProperty(userResult);

        // 重置 cookie
        CookieUtils.setCookie(request, response, "user", JsonUtils.objectToJson(usersVO),true);





        return IMOOCJSONResult.ok();
    }

    private Map<String, String> getErrors(BindingResult result) {

        List<FieldError> errorList = result.getFieldErrors();
        Map<String, String> map = new HashMap<>();
        for (FieldError error : errorList) {

            // 发生验证错误对应的某一个属性
            String errorField = error.getField();

            // 验证错误的信息
            String errorMsg = error.getDefaultMessage();

            map.put(errorField, errorMsg);
        }
        return map;
    }

    private Users setNullProperty(Users userResult) {

        userResult.setPassword(null);
        userResult.setMobile(null);
        userResult.setEmail(null);
        userResult.setCreatedTime(null);
        userResult.setUpdatedTime(null);
        userResult.setBirthday(null);
        return userResult;
    }
}
