package ru.iammaxim.ourlang.parser;

/**
 * Created by maxim on 2/12/17 at 2:15 PM.
 */
public enum TokenType {
    IDENTIFIER,
    DELIMITER, // ;,
    BRACE_OPEN, // {
    BRACE_CLOSE, // }
    PAREN_OPEN, // (
    PAREN_CLOSE, // )
    BRACKET_OPEN, // [
    BRACKET_CLOSE, // ]
    OPERATOR, // +-*/= != < <= == >= >
    KEYWORD,
    SCOPE_PARENS, // multiple expressions in parens
    SCOPE_BRACES, // multiple expressions in braces
    SCOPE_BRACKETS, // multiple expressions in brackets
    NEW_LINE
}
