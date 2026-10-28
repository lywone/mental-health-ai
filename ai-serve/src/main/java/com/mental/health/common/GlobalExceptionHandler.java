package com.mental.health.common;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

/**
 * 全局异常处理：业务异常/参数校验异常返回 HTTP 200 + 业务码，兜底异常返回 HTTP 500
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：HTTP 200 + 业务错误码 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    /** @RequestBody 校验失败：1101，message 带字段明细 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValid(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getAllErrors().stream()
                .map(err -> err instanceof FieldError fe
                        ? fe.getField() + ": " + fe.getDefaultMessage()
                        : err.getObjectName() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("；"));
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), ErrorCode.PARAM_ERROR.getMessage() + "：" + detail);
    }

    /** 表单绑定校验失败：1101 */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBind(BindException e) {
        String detail = e.getBindingResult().getAllErrors().stream()
                .map(err -> err instanceof FieldError fe
                        ? fe.getField() + ": " + fe.getDefaultMessage()
                        : err.getObjectName() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("；"));
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), ErrorCode.PARAM_ERROR.getMessage() + "：" + detail);
    }

    /** @RequestParam / @PathVariable 校验失败：1101 */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraint(ConstraintViolationException e) {
        String detail = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("；"));
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), ErrorCode.PARAM_ERROR.getMessage() + "：" + detail);
    }

    /** 上传文件超过 multipart 总限制：1302 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUpload(MaxUploadSizeExceededException e) {
        return Result.error(ErrorCode.FILE_TOO_LARGE);
    }

    /** 兜底：HTTP 500 + code 500 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统内部错误", e);
        return Result.error(ErrorCode.INTERNAL_ERROR);
    }
}
