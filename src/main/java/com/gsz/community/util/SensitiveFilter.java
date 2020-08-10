package com.gsz.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);
    //替换符号
    private static final String REPLACEMENT = "**";
    //根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init(){
        //构造该类之后自动调用本函数
        try(InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));) {
            String keyWord;
            while((keyWord =reader.readLine())!=null){
                //将keyword添加到前缀树中
                this.addKeyword(keyWord);
            }
        } catch (IOException e){
            logger.error("加载敏感词文件失败" + e.getMessage());
        }
    }

    private void addKeyword(String keyword){
        TrieNode temp = rootNode;
        for(int i=0;i<keyword.length();i++){
            char c = keyword.charAt(i);
            TrieNode subNode =  temp.getSubNode(c);
            //没有的话创建一个挂上
            if(subNode == null){
                subNode = new TrieNode();
                temp.addSubNode(c,subNode);
            }
            temp = subNode;
            //结束标识，表示到当前节点为止是一个敏感词
            if(i==keyword.length()-1){
                temp.setKeyWordEnd(true);
            }
        }
    }

    //过滤敏感词
    //参数为待过滤的文本
    //返回的为过滤的结果
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }
        //过滤依赖三个指针
        TrieNode temp = rootNode;
        int begin = 0;
        int position = 0;
        //结果
        StringBuilder result = new StringBuilder();

        while(position < text.length()){
            char c = text.charAt(position);
            //跳过符号
            if(isSymbol(c)){
                //若指针1处于根节点，将此符号计入结果，让指针2向下走一步
                if(temp == rootNode){
                    result.append(c);
                    begin++;
                }
                //无论符号在开头或中间，指针3都向下走一步
                position++;
                continue;
            }
            //检查下级节点
            temp = temp.getSubNode(c);
            if(temp == null){
                //以begin为开头的字符串不是敏感词
                result.append(text.charAt(begin));
                begin ++;
                position = begin;
                temp = rootNode;
            }else if(temp.isKeyWordEnd()){
                //发现敏感词，将begin到position字符串替换掉
                result.append(REPLACEMENT);
                //进入下一个位置
                position ++;
                begin = position;
                temp = rootNode;
            }else{
                //检查下一个字符
                position++;
            }
        }
        //将最后一批字符计入结果
        result.append(text.substring(begin));
        return result.toString();
    }

    private boolean isSymbol(Character c){
        //判断是不是普通的字符
        //第二个是东亚范围文字
        return !CharUtils.isAsciiAlphanumeric(c) && (c<0x2E80 || c>0x9FFF);
    }

    //前缀树结构
    //关键是标记是否为结尾
    private class TrieNode{
        //关键词结束的标识
        private boolean isKeyWordEnd = false;
        //子节点，key是下级字符，value是下级节点
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }

        public void addSubNode(Character c, TrieNode node){
            subNodes.put(c, node);
        }
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }

    }


}
