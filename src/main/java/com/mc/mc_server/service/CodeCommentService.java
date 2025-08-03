package com.mc.mc_server.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CodeCommentService {
    
    /**
     * 코드 diff를 분석해서 AI 주석을 생성합니다.
     */
    public String generateCommentsForCode(String filename, String patch) {
        if (patch == null || patch.isEmpty()) {
            return null;
        }
        
        StringBuilder commentedCode = new StringBuilder();
        String[] lines = patch.split("\n");
        
        for (String line : lines) {
            if (line.startsWith("@@")) {
                // diff 헤더는 그대로 유지
                commentedCode.append(line).append("\n");
            } else if (line.startsWith("+") && !line.equals("+")) {
                // 추가된 코드 라인에 주석 생성
                String addedLine = line.substring(1); // '+' 제거
                String comment = generateCommentForLine(addedLine, filename);
                
                commentedCode.append(line).append("\n");
                if (comment != null && !comment.isEmpty()) {
                    commentedCode.append("+ ").append(comment).append("\n");
                }
            } else {
                // 기존 라인들은 그대로 유지
                commentedCode.append(line).append("\n");
            }
        }
        
        return commentedCode.toString();
    }
    
    /**
     * 단일 코드 라인에 대한 주석을 생성합니다.
     */
    public String generateCommentForLine(String codeLine, String filename) {
        String trimmed = codeLine.trim();
        
        if (trimmed.isEmpty() || trimmed.startsWith("//") || trimmed.startsWith("/*")) {
            return null; // 이미 주석이거나 빈 라인
        }
        
        // Java 파일 분석
        if (filename.endsWith(".java")) {
            return generateJavaComment(trimmed);
        }
        
        // JavaScript/TypeScript 파일 분석
        if (filename.endsWith(".js") || filename.endsWith(".ts") || filename.endsWith(".jsx") || filename.endsWith(".tsx")) {
            return generateJavaScriptComment(trimmed);
        }
        
        // Python 파일 분석
        if (filename.endsWith(".py")) {
            return generatePythonComment(trimmed);
        }
        
        // CSS 파일 분석
        if (filename.endsWith(".css") || filename.endsWith(".scss")) {
            return generateCSSComment(trimmed);
        }
        
        return generateGenericComment(trimmed);
    }
    
    private String generateJavaComment(String codeLine) {
        // 클래스 선언
        if (codeLine.contains("class ") && codeLine.contains("{")) {
            return "// 새로운 클래스 정의";
        }
        
        // 메서드 선언
        if (codeLine.contains("public ") || codeLine.contains("private ") || codeLine.contains("protected ")) {
            if (codeLine.contains("(") && codeLine.contains(")")) {
                return "// 메서드 정의";
            }
        }
        
        // 인터페이스 확장
        if (codeLine.contains("extends") && codeLine.contains("Repository")) {
            return "// 레포지토리 인터페이스 확장으로 데이터 액세스 기능 추가";
        }
        
        // 변수 선언
        if (codeLine.contains("private final") || codeLine.contains("private ")) {
            return "// 클래스 멤버 변수 선언";
        }
        
        // Import 문
        if (codeLine.startsWith("import ")) {
            return "// 필요한 라이브러리 import";
        }
        
        // List 관련
        if (codeLine.contains("List<") && codeLine.contains("find")) {
            return "// 특정 조건으로 리스트 조회 메서드 추가";
        }
        
        return "// 코드 로직 추가";
    }
    
    private String generateJavaScriptComment(String codeLine) {
        // 함수 선언
        if (codeLine.contains("function ") || codeLine.contains("const ") && codeLine.contains("=>")) {
            return "// 새로운 함수 정의";
        }
        
        // 변수 선언
        if (codeLine.contains("const ") || codeLine.contains("let ") || codeLine.contains("var ")) {
            return "// 변수 선언";
        }
        
        // React 관련
        if (codeLine.contains("useState") || codeLine.contains("useEffect")) {
            return "// React 훅 사용";
        }
        
        // 이벤트 핸들러
        if (codeLine.contains("onClick") || codeLine.contains("onChange") || codeLine.contains("onSubmit")) {
            return "// 이벤트 핸들러 정의";
        }
        
        return "// JavaScript 로직 추가";
    }
    
    private String generatePythonComment(String codeLine) {
        // 함수 정의
        if (codeLine.startsWith("def ")) {
            return "# 새로운 함수 정의";
        }
        
        // 클래스 정의
        if (codeLine.startsWith("class ")) {
            return "# 새로운 클래스 정의";
        }
        
        // Import 문
        if (codeLine.startsWith("import ") || codeLine.startsWith("from ")) {
            return "# 필요한 모듈 import";
        }
        
        return "# Python 로직 추가";
    }
    
    private String generateCSSComment(String codeLine) {
        // 클래스 선택자
        if (codeLine.startsWith(".")) {
            return "/* 새로운 CSS 클래스 스타일 */";
        }
        
        // ID 선택자
        if (codeLine.startsWith("#")) {
            return "/* ID 선택자 스타일 */";
        }
        
        // 속성 정의
        if (codeLine.contains(":") && !codeLine.contains("/*")) {
            return "/* 스타일 속성 정의 */";
        }
        
        return "/* CSS 스타일 추가 */";
    }
    
    private String generateGenericComment(String codeLine) {
        return "// 코드 수정사항";
    }
    
    /**
     * 전체 파일의 주석을 정리하고 포맷팅합니다.
     */
    public String formatCommentedCode(String commentedCode) {
        if (commentedCode == null || commentedCode.isEmpty()) {
            return commentedCode;
        }
        
        // 중복 주석 제거 및 포맷팅
        String[] lines = commentedCode.split("\n");
        StringBuilder formatted = new StringBuilder();
        String lastComment = "";
        
        for (String line : lines) {
            if (line.trim().startsWith("//") || line.trim().startsWith("/*") || line.trim().startsWith("#")) {
                // 중복 주석 체크
                if (!line.equals(lastComment)) {
                    formatted.append(line).append("\n");
                    lastComment = line;
                }
            } else {
                formatted.append(line).append("\n");
                lastComment = "";
            }
        }
        
        return formatted.toString();
    }
}
