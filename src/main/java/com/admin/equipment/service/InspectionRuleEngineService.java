package com.admin.equipment.service;

import com.admin.equipment.model.InspectionTemplateItem;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InspectionRuleEngineService {

    public static class ItemJudgmentResult {
        private boolean passed;
        private boolean hasValue;
        private String message;

        public ItemJudgmentResult(boolean passed, boolean hasValue, String message) {
            this.passed = passed;
            this.hasValue = hasValue;
            this.message = message;
        }

        public boolean isPassed() { return passed; }
        public boolean hasValue() { return hasValue; }
        public String getMessage() { return message; }
    }

    public ItemJudgmentResult judgeItem(InspectionTemplateItem item, String textValue,
                                        Double numericValue, Boolean boolValue) {
        if (!hasAnyValue(item.getType(), textValue, numericValue, boolValue)) {
            if (item.getRequired()) {
                return new ItemJudgmentResult(false, false, "必填项未填写");
            }
            return new ItemJudgmentResult(true, false, "非必填项，未填写");
        }

        switch (item.getType()) {
            case "numeric":
                return judgeNumeric(item, numericValue);
            case "option":
                return judgeOption(item, textValue);
            case "boolean":
                return judgeBoolean(item, boolValue);
            case "text":
                return new ItemJudgmentResult(true, true, "文本项已填写");
            default:
                return new ItemJudgmentResult(true, true, "已填写");
        }
    }

    private boolean hasAnyValue(String type, String text, Double num, Boolean bool) {
        switch (type) {
            case "numeric":
                return num != null;
            case "boolean":
                return bool != null;
            case "option":
            case "text":
                return text != null && !text.trim().isEmpty();
            default:
                return text != null && !text.trim().isEmpty();
        }
    }

    private ItemJudgmentResult judgeNumeric(InspectionTemplateItem item, Double value) {
        if (value == null) {
            return new ItemJudgmentResult(!item.getRequired(), false, "数值为空");
        }

        Double min = item.getNumericMin();
        Double max = item.getNumericMax();

        if (min == null && max == null) {
            return new ItemJudgmentResult(true, true, "数值已填写，无区间限制");
        }

        boolean passed = true;
        StringBuilder msg = new StringBuilder();
        msg.append("数值: ").append(value);

        if (min != null && value < min) {
            passed = false;
            msg.append("，低于最小值 ").append(min);
        }
        if (max != null && value > max) {
            passed = false;
            msg.append("，高于最大值 ").append(max);
        }

        if (passed) {
            msg.append("，在正常区间内");
        }

        return new ItemJudgmentResult(passed, true, msg.toString());
    }

    private ItemJudgmentResult judgeOption(InspectionTemplateItem item, String value) {
        if (value == null || value.trim().isEmpty()) {
            return new ItemJudgmentResult(!item.getRequired(), false, "选项为空");
        }

        String passOptionsStr = item.getPassOptions();
        if (passOptionsStr == null || passOptionsStr.trim().isEmpty()) {
            return new ItemJudgmentResult(true, true, "已选择: " + value + "，无合格值限制");
        }

        List<String> passOptions = parseCsv(passOptionsStr);
        boolean passed = passOptions.contains(value.trim());
        String msg = "已选择: " + value + (passed ? "，合格" : "，不合格，合格值为: " + passOptionsStr);

        return new ItemJudgmentResult(passed, true, msg);
    }

    private ItemJudgmentResult judgeBoolean(InspectionTemplateItem item, Boolean value) {
        if (value == null) {
            return new ItemJudgmentResult(!item.getRequired(), false, "布尔值为空");
        }

        Boolean passValue = item.getBooleanPassValue();
        if (passValue == null) {
            passValue = true;
        }

        boolean passed = value.equals(passValue);
        String msg = "值: " + value + (passed ? "，合格" : "，不合格");

        return new ItemJudgmentResult(passed, true, msg);
    }

    public List<String> parseCsv(String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public boolean isOrderPassed(List<Boolean> itemPassedList) {
        if (itemPassedList == null || itemPassedList.isEmpty()) {
            return false;
        }
        return itemPassedList.stream().allMatch(p -> p != null && p);
    }
}
