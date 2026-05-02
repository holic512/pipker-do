package org.example.backend.shared.account.controller;

import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.shared.account.dto.vip.AdminVipRecordResponse;
import org.example.backend.shared.account.dto.vip.AdminVipUserResponse;
import org.example.backend.shared.account.dto.vip.VipCardGroupResponse;
import org.example.backend.shared.account.dto.vip.VipCardGroupStatusUpdateRequest;
import org.example.backend.shared.account.dto.vip.VipCardGroupUpsertRequest;
import org.example.backend.shared.account.dto.vip.VipCardKeyBatchCreateRequest;
import org.example.backend.shared.account.dto.vip.VipCardKeyBatchCreateResponse;
import org.example.backend.shared.account.dto.vip.VipCardKeyBatchResponse;
import org.example.backend.shared.account.dto.vip.VipCardKeyBatchVoidResponse;
import org.example.backend.shared.account.dto.vip.VipCardKeyResponse;
import org.example.backend.shared.account.dto.vip.VipManualGrantRequest;
import org.example.backend.shared.account.dto.vip.VipVoidRequest;
import org.example.backend.shared.account.service.VipManagementService;
import org.example.backend.shared.admin.support.AdminSecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI 索引: 后台 VIP 卡组、兑换 Key 与用户会员管理接口。
 */
@RestController
@RequestMapping("/api/admin/vip")
public class AdminVipController {

    private final VipManagementService vipManagementService;
    private final ApiResponseFactory responseFactory;

    public AdminVipController(VipManagementService vipManagementService, ApiResponseFactory responseFactory) {
        this.vipManagementService = vipManagementService;
        this.responseFactory = responseFactory;
    }

    @GetMapping("/card-groups")
    public ApiResponse<List<VipCardGroupResponse>> listCardGroups(@RequestParam(required = false) String keyword,
                                                                  @RequestParam(required = false) Integer status) {
        return responseFactory.success(vipManagementService.listCardGroups(AdminSecurityContext.requireAdminId(), keyword, status));
    }

    @PostMapping("/card-groups")
    public ApiResponse<VipCardGroupResponse> createCardGroup(@RequestBody VipCardGroupUpsertRequest request) {
        return responseFactory.success(vipManagementService.createCardGroup(AdminSecurityContext.requireAdminId(), request));
    }

    @PutMapping("/card-groups/{groupId}")
    public ApiResponse<VipCardGroupResponse> updateCardGroup(@PathVariable Long groupId,
                                                             @RequestBody VipCardGroupUpsertRequest request) {
        return responseFactory.success(vipManagementService.updateCardGroup(AdminSecurityContext.requireAdminId(), groupId, request));
    }

    @PutMapping("/card-groups/{groupId}/status")
    public ApiResponse<VipCardGroupResponse> updateCardGroupStatus(@PathVariable Long groupId,
                                                                   @RequestBody VipCardGroupStatusUpdateRequest request) {
        return responseFactory.success(vipManagementService.updateCardGroupStatus(AdminSecurityContext.requireAdminId(), groupId, request));
    }

    @PostMapping("/card-groups/{groupId}/keys/batch")
    public ApiResponse<VipCardKeyBatchCreateResponse> batchCreateKeys(@PathVariable Long groupId,
                                                                      @RequestBody VipCardKeyBatchCreateRequest request) {
        return responseFactory.success(vipManagementService.batchCreateKeys(AdminSecurityContext.requireAdminId(), groupId, request));
    }

    @GetMapping("/card-key-batches")
    public ApiResponse<List<VipCardKeyBatchResponse>> listCardKeyBatches(@RequestParam Long groupId) {
        return responseFactory.success(vipManagementService.listCardKeyBatches(AdminSecurityContext.requireAdminId(), groupId));
    }

    @PutMapping("/card-key-batches/{batchNo}/void")
    public ApiResponse<VipCardKeyBatchVoidResponse> voidCardKeyBatch(@PathVariable String batchNo,
                                                                     @RequestParam Long groupId,
                                                                     @RequestBody VipVoidRequest request) {
        return responseFactory.success(vipManagementService.voidCardKeyBatch(AdminSecurityContext.requireAdminId(), groupId, batchNo, request));
    }

    @GetMapping("/card-keys")
    public ApiResponse<List<VipCardKeyResponse>> listCardKeys(@RequestParam(required = false) Long groupId,
                                                              @RequestParam(required = false) Integer status,
                                                              @RequestParam(required = false) Long userId,
                                                              @RequestParam(required = false) String keyword,
                                                              @RequestParam(required = false) String batchNo) {
        return responseFactory.success(vipManagementService.listCardKeys(AdminSecurityContext.requireAdminId(), groupId, status, userId, keyword, batchNo));
    }

    @PutMapping("/card-keys/{keyId}/void")
    public ApiResponse<VipCardKeyResponse> voidCardKey(@PathVariable Long keyId,
                                                       @RequestBody VipVoidRequest request) {
        return responseFactory.success(vipManagementService.voidCardKey(AdminSecurityContext.requireAdminId(), keyId, request));
    }

    @DeleteMapping("/card-keys/{keyId}")
    public ApiResponse<Void> deleteCardKey(@PathVariable Long keyId) {
        vipManagementService.deleteCardKey(AdminSecurityContext.requireAdminId(), keyId);
        return responseFactory.success(null);
    }

    @GetMapping("/users")
    public ApiResponse<List<AdminVipUserResponse>> listVipUsers(@RequestParam(required = false) String keyword,
                                                                @RequestParam(required = false) Integer vipStatus) {
        return responseFactory.success(vipManagementService.listVipUsers(AdminSecurityContext.requireAdminId(), keyword, vipStatus));
    }

    @GetMapping("/users/{userId}/vip-records")
    public ApiResponse<List<AdminVipRecordResponse>> listVipRecords(@PathVariable Long userId) {
        return responseFactory.success(vipManagementService.listVipRecords(AdminSecurityContext.requireAdminId(), userId));
    }

    @PostMapping("/users/{userId}/vip")
    public ApiResponse<AdminVipRecordResponse> grantVip(@PathVariable Long userId,
                                                        @RequestBody VipManualGrantRequest request) {
        return responseFactory.success(vipManagementService.grantVip(AdminSecurityContext.requireAdminId(), userId, request));
    }

    @PutMapping("/users/{userId}/vip/{vipId}/void")
    public ApiResponse<AdminVipRecordResponse> voidVipRecord(@PathVariable Long userId,
                                                             @PathVariable Long vipId,
                                                             @RequestBody VipVoidRequest request) {
        return responseFactory.success(vipManagementService.voidVipRecord(AdminSecurityContext.requireAdminId(), userId, vipId, request));
    }
}
