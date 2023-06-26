package com.nowcoder.community.util;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.CharUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 敏感词替换符
    private static final String REPLACEMENT = "***";

    // 前缀树的根节点
    private final TrieNode rootNode = new TrieNode();

    // 初始化方法
    // 在bean初始化之后，调用此方法，用于初始化前缀树
    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
        ) {
            if (is == null) {
                logger.error("加载敏感词文件失败！");
            } else {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                ) {
                    String keyword;
                    while ((keyword = reader.readLine()) != null) {
                        // 添加到前缀树
                        this.addKeyword(keyword);
                    }
                } catch (IOException e) {
                    logger.error("加载敏感词文件失败：" + e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }
    }

    // 将一个敏感词添加到前缀树中
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            // 获取当前字符
            char c = keyword.charAt(i);
            // 获取当前字符的子节点
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null) {
                // 初始化子节点
                subNode = new TrieNode();
                // 将子节点添加到当前节点的子节点中
                tempNode.addSubNode(c, subNode);
            }
            // 指向子节点，进入下一轮循环
            tempNode = subNode;
            // 设置结束标识
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    // 判断是否为符号
    private boolean isSymbol(Character c) {
        // 0x2E80-0x9FFF 东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    // 过滤敏感词
    public String filter(String text) {
        if (text == null) {
            return null;
        }

        // 指针1
        TrieNode tempNode = rootNode;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;
        // 结果
        StringBuilder sb = new StringBuilder();
        // 注意：这里的循环条件是begin < text.length()，而不是position < text.length()
        // 敏感词fabcd和abc，防止fabc检测不出abc
        while (begin < text.length()) {
            // 指针3越界, 说明以text.charAt(begin)开头的词不是敏感词
            if (position == text.length()) {
                sb.append(text.charAt(begin));
                // 复位
                position = ++begin;
                tempNode = rootNode;
                continue;
            }
            char c = text.charAt(position);
            // 跳过符号
            if (isSymbol(c)) {
                // 若指针1处于根节点，说明还位于开头，直接跳过这个符号
                // 将此符号计入结果，让指针2向下走一步
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                // 无论符号在开头或中间，指针3都向下走一步
                position++;
                continue;
            }
            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // 以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                // 进入下一个位置
                position = ++begin;
                // 重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // 发现敏感词，将begin~position字符串替换掉
                sb.append(REPLACEMENT);
                // 进入下一个位置
                begin = ++position;
                // 重新指向根节点
                tempNode = rootNode;
            } else if (position + 1 == text.length()) {
                // 特殊情况
                // 虽然position指向的字符在树中存在，但不是敏感词结尾，并且position到了目标字符串末尾（这个重要）
                // 因此begin-position之间的字符串不是敏感词 但begin+1-position之间的不一定不是敏感词
                // 所以只将begin指向的字符放入过滤结果
                sb.append(text.charAt(begin));
                // position和begin都指向begin+1
                position = ++begin;
                // 再次过滤
                tempNode = rootNode;
            } else {
                // position指向的字符在树中存在，但不是敏感词结尾，并且position没有到目标字符串末尾
                // 检查下一个字符
                position++;
            }
        }
        return sb.toString();
    }

    // 前缀树
    @Getter
    @Setter
    @ToString
    private static class TrieNode {

        // 关键词结束标识
        private boolean isKeywordEnd = false;

        // 子节点（key是下级字符，value是下级节点）
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        // 添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}