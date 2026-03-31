package com.suncan.english.module.reward.controller;

import com.suncan.english.module.reward.service.RewardService;
import com.suncan.english.module.reward.vo.BadgeVO;
import com.suncan.english.module.reward.vo.PointsVO;
import com.suncan.english.module.reward.vo.RankVO;
import com.suncan.english.shared.common.Result;
import com.suncan.english.shared.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户端-奖励模块", description = "积分、排行和徽章接口")
@RestController
@RequestMapping("/api/reward")
public class RewardController {

    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @Operation(summary = "查询总积分", description = "查询当前用户总积分", security = {@SecurityRequirement(name = "Authorization")})
    @GetMapping("/points")
    public Result<PointsVO> points() {
        Long userId = UserContext.getUserId();
        return Result.success(rewardService.getPoints(userId));
    }

    @Operation(summary = "积分排行", description = "查询排行榜与我的排名", security = {@SecurityRequirement(name = "Authorization")})
    @GetMapping("/rank")
    public Result<RankVO> rank() {
        Long userId = UserContext.getUserId();
        return Result.success(rewardService.getRank(userId));
    }

    @Operation(summary = "徽章列表", description = "查询徽章列表", security = {@SecurityRequirement(name = "Authorization")})
    @GetMapping("/badges")
    public Result<List<BadgeVO>> badges() {
        Long userId = UserContext.getUserId();
        return Result.success(rewardService.listBadges(userId));
    }
}