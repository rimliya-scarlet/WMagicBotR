package com.whitemagic2014.command.impl.group.funny;

import com.whitemagic2014.command.impl.group.NoAuthCommand;
import com.whitemagic2014.dao.UserPlanDao;
import com.whitemagic2014.pojo.CommandProperties;
import com.whitemagic2014.pojo.UserPlan;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 计划表任务
 * @author: magic chen
 * @date: 2020/8/21 17:21
 **/
@Component
public class PlanCommand extends NoAuthCommand {

    @Autowired
    UserPlanDao upd;

    @Override
    public CommandProperties properties() {
        return new CommandProperties("plan", "计划");
    }

    @Override
    protected Message executeHandle(Member sender, ArrayList<String> args, MessageChain messageChain, Group subject) {

        String uid = String.valueOf(sender.getId());

        At at = new At(sender);

        if (args.size() == 0) {
            return at.plus(find(uid, ""));
        }
        String com = args.get(0).toString();

        try {
            if (com.equals("delete") || com.equals("删除") || com.equals("del")) {
                return at.plus(deleteByName(uid, (args.get(1)).toString()));
            } else if (com.equals("new") || com.equals("新建") || com.equals("创建")) {
                return at.plus(addNewPlan(uid, args.get(1).toString(), Integer.valueOf(args.get(2))));
            } else if (com.equals("add") || com.equals("新增")) {
                return at.plus(execPlan(uid, args.get(1).toString(), Integer.valueOf(args.get(2))));
            } else if (com.equals("check") || com.equals("查询") || com.equals("look")) {
                return at.plus(find(uid, ""));
            } else if (com.equals("update") || com.equals("更新")) {
                return at.plus(updatePlan(uid, args.get(1).toString(), Integer.valueOf(args.get(2))));
            }
        } catch (Exception e) {
            return new PlainText(help());
        }
        return new PlainText(help());
    }

    /**
     * @param uid
     * @param itemName
     * @param planNum
     * @return
     * @Description: 新增计划
     * @author: chenhaoyu
     * @time:Jul 28, 2020 5:30:43 PM
     */
    private String addNewPlan(String uid, String itemName, int planNum) {
        try {
            UserPlan plan = new UserPlan();
            plan.setItemName(itemName);
            plan.setUid(uid);
            plan.setPlanNum(planNum);
            plan.setNowNum(0);
            upd.instertPlan(plan);
            return "计划添加成功";
        } catch (Exception e) {
            return "计划添加失败:\n" + e.getMessage();
        }
    }

    private String deleteByName(String uid, String itemName) {
        List<UserPlan> plans = upd.findPlans(uid, itemName);
        if (plans.isEmpty()) {
            return "无需删除";
        } else {
            return delete(plans.get(0).getId());
        }
    }

    private String delete(int id) {
        try {
            upd.deletePlan(id);
            return "计划删除成功";
        } catch (Exception e) {
            return "计划添加失败:\n" + e.getMessage();
        }
    }

    private String find(String uid, String itemName) {
        String result = "当前计划:\n";
        List<UserPlan> plans = upd.findPlans(uid, itemName);
        for (UserPlan plan : plans) {
            result += plan.getItemName() + " " + plan.getPlanNum() + "(" + plan.getNowNum() + ") 个\n";
        }
        return result;
    }

    private String execPlan(String uid, String itemName, int addNum) {
        List<UserPlan> plans = upd.findPlans(uid, itemName);
        if (plans.isEmpty()) {
            return "暂时还没有该任务计划,请先创建计划";
        }

        UserPlan plan = plans.get(0);

        int total = plan.getPlanNum();
        int now = plan.getNowNum();

        if ((now + addNum) < total) {
            UserPlan update = new UserPlan();
            update.setId(plan.getId());
            update.setNowNum(total - now - addNum);
            upd.updatePlan(update);
            return "任务更新: " + plan.getItemName() + " 当前 " + (now + addNum) + " 个,剩余 " + (total - now - addNum) + " 个 "
                    + plan.getItemName();
        } else if ((now + addNum) > total) {
            upd.deletePlan(plan.getId());
            return "厉害了！超额完成任务" + plan.getItemName() + "(" + total + ") " + (now + addNum - total) + "个";
        } else {
            upd.deletePlan(plan.getId());
            return "已经完成任务: " + total + " 个 " + plan.getItemName();
        }

    }

    //更新计划 上一个exec的另一种版本
    private String updatePlan(String uid, String itemName, int newNum) {
        List<UserPlan> plans = upd.findPlans(uid, itemName);
        if (plans.isEmpty()) {
            return "暂时还没有该任务计划,请先创建计划";
        }
        UserPlan plan = plans.get(0);

        int total = plan.getPlanNum();
        if (newNum > total) {
            upd.deletePlan(plan.getId());
            return "已经完成任务: " + total + " 个 " + plan.getItemName();
        } else {
            UserPlan update = new UserPlan();
            update.setId(plan.getId());
            update.setNowNum(newNum);
            upd.updatePlan(update);
            return "任务更新: " + plan.getItemName() + " 当前 " + newNum + " 个,剩余 " + (total - newNum) + " 个 "
                    + plan.getItemName();
        }
    }


    private String help() {
        String result = "计划表功能:\n"
                + "创建: [指令前缀][plan/计划] [new/新建/创建] [道具名] [目标数量]\n"
                + "新增: [指令前缀][plan/计划] [add/新增] [道具名] [新获得数量]\n"
                + "更新: [指令前缀][plan/计划] [update/更新] [道具名] [目前数量]\n"
                + "删除: [指令前缀][plan/计划] [delete/del/删除] [道具名]\n"
                + "查询: [指令前缀][plan/计划] [check/look/查询]";
        return result;
    }

}
