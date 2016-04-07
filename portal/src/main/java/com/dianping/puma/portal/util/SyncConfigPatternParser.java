package com.dianping.puma.portal.util;

import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyncConfigPatternParser {

    private final static Pattern REGEX_PATTERN = Pattern.compile("(.*)(\\[\\d+\\-\\d+\\])(.*)");
    private final static Pattern PARTITION_PATTERN = Pattern.compile("#partition\\((.*)(\\[.*\\])(.*)\\)");
    private final static Pattern PARTITION_ARITHMETIC_PATTERN = Pattern.compile("\\$(\\w+)([\\%\\-\\/])(\\d+)");

    /**
     * @param arg 例如test_[0-31]
     * @return
     */
    public static String[] regex(String arg) {
        //解析出arg中的[x-x]
        String[] re;
        Matcher matcher = REGEX_PATTERN.matcher(arg);
        if (matcher.matches()) {
            String group1 = matcher.group(1);
            String group2 = matcher.group(2);
            String group3 = matcher.group(3);
            group2 = StringUtils.replaceChars(group2, "[]", "");
            String[] numbers = group2.split("-");
            int begin = Integer.parseInt(numbers[0]);
            int end = Integer.parseInt(numbers[1]);
            int count = end - begin + 1;
            re = new String[count];
            for (int i = begin; i <= end; i++) {
                re[i - begin] = group1 + i + group3;
            }
            return re;
        } else {
            throw new IllegalArgumentException("Not match :" + arg);
        }
    }

    /**
     * 将自动分表的表名(如#partition(test_[$uid/2]))，根据RowChangedEvent中的具体列值，解析为具体的表名称
     */
    public static String partition(String arg, Map<String, ColumnInfo> columns) {
        //解析出arg中的[$uid/2]
        Matcher matcher = PARTITION_PATTERN.matcher(arg);
        if (matcher.matches()) {
            String leftStr = matcher.group(1);
            String partionStr = matcher.group(2);
            String rightStr = matcher.group(3);
            partionStr = StringUtils.replaceChars(partionStr, "[]", "");
            System.out.println(partionStr);
            Matcher arithmeticMatcher = PARTITION_ARITHMETIC_PATTERN.matcher(partionStr);
            int numberResult = 0;
            if (arithmeticMatcher.matches()) {
                String columnVar = arithmeticMatcher.group(1);
                char operation = arithmeticMatcher.group(2).charAt(0);
                int number = Integer.parseInt(arithmeticMatcher.group(3));
                ColumnInfo columnInfo = columns.get(columnVar);
                long columnValue = Long.parseLong(columnInfo.getNewValue().toString());
                switch (operation) {
                    case '%':
                        numberResult = (int) (columnValue % number);
                        break;
                    case '-':
                        numberResult = (int) (columnValue - number);
                        break;
                    case '/':
                        numberResult = (int) (columnValue / number);
                        break;
                }
            } else {
                throw new IllegalArgumentException("Not match :" + partionStr);
            }

            return leftStr + numberResult + rightStr;
        } else {
            throw new IllegalArgumentException("Not match :" + arg);
        }
    }

    public static void main(String[] args) {
        //        System.out.println(Arrays.toString(regex("[0-9]")));
        //        System.out.println(Arrays.toString(regex("test_[0-9]")));
        //        System.out.println(Arrays.toString(regex("test_[0-9]_dasd")));
        //        System.out.println(partition("test_[$uid/2]_dasd"));
    }

}
