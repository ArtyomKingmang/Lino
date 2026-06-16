package lexer

import "fmt"

const (
	NUM = iota
	ID
	EOF
	COMPARATOR
	ASSIGN
	IF
	WHILE
	LBRACE
	RBRACE
	RETURN
	REPEAT
	COMMENT
	TYPE
	VOID
	PRINT
	LITERAL
	COLON
	FUNC
)

type Token interface {
	GetTag() int
	Stringify() string
	String() string
}

type BaseToken struct {
	Tag int
}

func (t *BaseToken) GetTag() int {
	return t.Tag
}

func (t *BaseToken) Stringify() string {
	if t.Tag == ASSIGN {
		return "="
	}
	return string(rune(t.Tag))
}

func (t *BaseToken) String() string {
	return fmt.Sprintf("Token: %c", t.Tag)
}

type WordToken struct {
	BaseToken
	Lexeme string
}

func NewWordToken(tag int, lexeme string) *WordToken {
	return &WordToken{BaseToken{tag}, lexeme}
}

func (w *WordToken) Stringify() string {
	return w.Lexeme
}

func (w *WordToken) String() string {
	return fmt.Sprintf("Word: %s TokenType: %d", w.Lexeme, w.Tag)
}

type NumToken struct {
	BaseToken
	Value int
}

func NewNumToken(value int) *NumToken {
	return &NumToken{BaseToken{NUM}, value}
}

func (n *NumToken) Stringify() string {
	return fmt.Sprintf("%d", n.Value)
}

func (n *NumToken) String() string {
	return fmt.Sprintf("Number: %d", n.Value)
}

type LiteralToken struct {
	BaseToken
	Value string
}

func NewLiteralToken(value string) *LiteralToken {
	return &LiteralToken{BaseToken{LITERAL}, value}
}

func (l *LiteralToken) Stringify() string {
	return "\"" + l.Value + "\""
}

func (l *LiteralToken) String() string {
	return fmt.Sprintf("Literal String: \"%s\"", l.Value)
}

type OperatorToken struct {
	BaseToken
	Value string
}

func NewOperatorToken(value string) *OperatorToken {
	return &OperatorToken{BaseToken{COMPARATOR}, value}
}

func (o *OperatorToken) Stringify() string {
	return o.Value
}

func (o *OperatorToken) String() string {
	return fmt.Sprintf("Operator: %s", o.Value)
}
