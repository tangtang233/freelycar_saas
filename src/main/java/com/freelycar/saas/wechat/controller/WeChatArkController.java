package com.freelycar.saas.wechat.controller;

import com.freelycar.saas.basic.wrapper.ResultJsonObject;
import com.freelycar.saas.project.model.OrderObject;
import com.freelycar.saas.project.service.ConsumerOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author tangwei - Toby
 * @date 2019-01-25
 * @email toby911115@gmail.com
 */

@RestController
@RequestMapping("/wechat/ark")
public class WeChatArkController {
    @Autowired
    private ConsumerOrderService consumerOrderService;

    @GetMapping("/getActiveOrder")
    public ResultJsonObject getActiveOrder(@RequestParam String clientId) {
        return consumerOrderService.getActiveOrder(clientId);
    }

    @PostMapping("/orderService")
    public ResultJsonObject orderService(@RequestBody OrderObject orderObject) {
        return consumerOrderService.arkHandleOrder(orderObject);
    }


}