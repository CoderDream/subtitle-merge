package com.coderdream;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;


public class SubtitleUtil {

    static Logger logger = Logger.getLogger(SubtitleUtil.class.getName());

    public static void main(String[] args) throws IOException {
        if (args.length == 3) {
            merge(args[0], args[1], args[2]);
            logger.info(args[2] + " makes successful.");
        } else {
            logger.info("args length is not 3, please check and run again");
        }
    }

    private static void merge(String fileName1, String fileName2, String fileName) throws IOException {
        // Java8用流的方式读文件，更加高效
        // Files.lines(Paths.get("Subtitle_5.srt"), StandardCharsets.UTF_8).forEach(System.out::println);
//        String fileName1 = "Subtitle_1.srt";
//        String fileName2 = "Subtitle_5.srt";
//        String fileName = "Subtitle_merge.srt";
        List<Item> firstItems = SubtitleUtil.readFile(fileName1);
        List<Item> secondItems = SubtitleUtil.readFile(fileName2);
        List<Item> subtitleMergeItems = subtitleMerge(firstItems, secondItems);
//        for (Item item : subtitleMergeItems) {
//            System.out.println(item);
//        }
        writeBuffer(fileName, subtitleMergeItems);
    }

    /**
     * 写入文件
     *
     * @return
     * @throws IOException
     */
    public static File writeBuffer(String fileName, List<Item> subtitleMergeItems) throws IOException {
        if (!Files.exists(Paths.get(fileName))) Files.write(Paths.get(fileName), new byte[0]);
        File file = new File(fileName);
        FileOutputStream fos = new FileOutputStream(file);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
        for (Item item : subtitleMergeItems) {
            // System.out.println(item);
            writer.write(item.toMultiLine() + "\r\n");
        }

        writer.close();
        fos.close();
        return file;
    }


    public static List<Item> readFile(String fileName) {
        //read file into stream, try-with-resources
        List<Item> results = new ArrayList<>();
        Item item = null;
        Integer id = 0;
        String timeRange = "";
        String content = "";
        String contentNext = "";
        //Boolean brFlag = false;
        Set<String> timeRangeSet = new HashSet<>();
        try (Stream<String> stream = Files.lines(Paths.get(fileName), StandardCharsets.UTF_8)) {
            List<String> list = new ArrayList<String>();
            stream.forEach(str -> {
                list.add(str);
            });

            String[] lines = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                lines[i] = list.get(i);
            }

            for (int i = 0; i < lines.length; i++) {
                //System.out.println( intarray[i] );
                String str = lines[i];
                if ("".equals(str.trim())) {
                    item = new Item();
                    String prewLine3 = lines[i - 3];
                    // 不是数字，再往上找
                    if (!isNumeric(prewLine3)) {
                        String prewLine4 = lines[i - 4];
                        if (!isNumeric(prewLine4)) {
                            System.out.println("ERROR");
                        }
                        // 内容有两行，需要合并
                        else {
                            id = Integer.parseInt(lines[i - 4]);
                            timeRange = lines[i - 3];
                            content = lines[i - 2];
                            contentNext = lines[i - 1];
                            // 合并内容
                            content = content + " " + contentNext;
                        }
                    }
                    // 只有一行，不需合并
                    else {
                        id = Integer.parseInt(lines[i - 3]);
                        timeRange = lines[i - 2];
                        content = lines[i - 1];
                    }

                    item.setId(id);
                    item.setTimeRange(timeRange);
                    item.setContent(content);
                    if (!timeRangeSet.contains(timeRange)) {
                        // 加入返回列表
                        results.add(item);
                    }
                    // 去掉重复
                    timeRangeSet.add(timeRange);
                    //System.out.println(index + " br");
                } else {
                    //id
                    //System.out.println(index + str);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }

    public static List<Item> subtitleMerge(List<Item> firstItems, List<Item> secondItems) {
        List<Item> subtitleMergeItems = new ArrayList<>();
        Map<String, Item> secondMap = new HashMap<>();
        for (Item item : secondItems) {
            secondMap.put(item.getTimeRange(), item);
        }

        Item secondItem = null;
        String firstContent = "";
        String secondContent = "";
        for (Item item : firstItems) {
            secondItem = secondMap.get(item.getTimeRange());
            if (null != secondItem) {
                firstContent = item.getContent();
                secondContent = secondItem.getContent();
                item.setContent(firstContent);
                item.setSecondContent(secondContent);
            }
            subtitleMergeItems.add(item);
        }

        return subtitleMergeItems;
    }

    public static boolean isNumeric(String string) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(string).matches();
    }
}
