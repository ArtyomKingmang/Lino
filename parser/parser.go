package parser

import (
	"bytes"
	"fmt"
	"strconv"
	"strings"

	"github.com/kingmang/lino/lexer"
	"github.com/kingmang/lino/symbol"
)

type Parser struct {
	lexer      *lexer.Lexer
	token      lexer.Token
	output     strings.Builder
	env        *symbol.Environment
	console    strings.Builder
	errors     []int
	space      string
	hasError   bool
	line       int
	bufBuilder bytes.Buffer
}

func NewParser(l *lexer.Lexer) *Parser {
	p := &Parser{lexer: l}
	p.nextToken()
	return p
}

func (p *Parser) Program() error {
	p.env = nil
	p.output.WriteString(p.methods())

	errCount := len(p.errors)
	errWord := "errors"
	if errCount <= 1 {
		errWord = "error"
	}
	p.appendLineToConsole(fmt.Sprintf("Done with %d %s", errCount, errWord), errCount > 0)

	return nil
}

func (p *Parser) Output() string {
	return p.output.String()
}

func (p *Parser) Console() string {
	return p.console.String()
}

func (p *Parser) methods() string {
	var methods strings.Builder
	for {
		methods.WriteString(p.method())
		if p.token.GetTag() == lexer.EOF {
			break
		}
	}
	return methods.String()
}

func (p *Parser) method() string {
	saved := p.env
	p.env = symbol.NewEnvironment(p.env)
	tempSpace := p.space
	p.space += "\t"
	hasReturn := false

	p.match(lexer.FUNC)
	name := p.match(lexer.ID)
	p.match('(')
	params := p.optParams()
	p.match(')')
	p.match(lexer.COLON)

	returnType := "void"
	if p.token.GetTag() == lexer.TYPE {
		returnType = TranslateLexeme(p.match(lexer.TYPE))
		if p.token.GetTag() == '[' {
			returnType += p.match('[')
			returnType += p.match(']')
		}
		hasReturn = true
	} else if p.token.GetTag() == lexer.VOID {
		p.match(lexer.VOID)
		hasReturn = false
	} else {
		p.error(lexer.RETURN, "return")
		hasReturn = true
	}

	p.match(lexer.LBRACE)
	decelerations := p.decelerations()
	statements := p.optStatement()

	var optReturn strings.Builder
	optReturn.WriteString("\t")
	if hasReturn {
		optReturn.WriteString(p.space)
		optReturn.WriteString("\n\t")
		optReturn.WriteString(p.match(lexer.RETURN))
		optReturn.WriteString(" ")
		optReturn.WriteString(p.expr())
		optReturn.WriteString(";")
		optReturn.WriteString("\n\t")
	} else if p.token.GetTag() == lexer.RETURN {
		optReturn.WriteString(p.space)
		optReturn.WriteString(p.match(lexer.RETURN))
		optReturn.WriteString(";")
		optReturn.WriteString("\n\t")
	}
	p.match(lexer.RBRACE)

	p.env = saved
	p.space = tempSpace

	return fmt.Sprintf("%s%s(%s){\n%s%s%s\n}\n", returnType, name, params, decelerations, statements, optReturn.String())
}

func (p *Parser) optStatement() string {
	var optStatement strings.Builder
	for p.token.GetTag() != lexer.RETURN && p.token.GetTag() != lexer.RBRACE && p.token.GetTag() != lexer.EOF {
		optStatement.WriteString(p.statement())
	}
	return optStatement.String()
}

func (p *Parser) optParams() string {
	var optParams strings.Builder
	optParams.WriteString(p.param())
	for {
		if p.token.GetTag() == ',' {
			optParams.WriteString(p.match(','))
			optParams.WriteString(p.param())
		} else {
			return optParams.String()
		}
	}
}

func (p *Parser) decelerations() string {
	var decelerations strings.Builder
	for {
		if p.token.GetTag() == lexer.TYPE {
			decelerations.WriteString(p.deceleration())
		} else {
			break
		}
	}
	return decelerations.String()
}

func (p *Parser) deceleration() string {
	var decelration strings.Builder
	sym := &symbol.Symbol{}
	typeName := p.match(lexer.TYPE)
	sym.Type = typeName
	decelration.WriteString(TranslateLexeme(typeName))
	id := p.match(lexer.ID)
	decelration.WriteString(id)
	if p.token.GetTag() == '[' {
		decelration.WriteString(p.match('['))
		decelration.WriteString(p.match(']'))
	}
	if p.token.GetTag() == lexer.ASSIGN {
		decelration.WriteString(p.match(lexer.ASSIGN))
		value := p.expr()
		decelration.WriteString(value)
		sym.Value = value
	}
	if p.env.Get(id) == nil {
		p.env.Put(id, sym)
	} else {
		p.appendLineToConsole("Error: Variable "+id+" is already defined in the scope", true)
	}
	decelration.WriteString(";")
	return p.space + decelration.String() + "\n"
}

func (p *Parser) param() string {
	var param strings.Builder
	if p.token.GetTag() == lexer.TYPE {
		sym := &symbol.Symbol{}
		typeName := p.match(lexer.TYPE)
		sym.Type = typeName
		param.WriteString(TranslateLexeme(typeName))
		id := p.match(lexer.ID)
		param.WriteString(id)
		if p.token.GetTag() == '[' {
			param.WriteString(p.match('['))
			param.WriteString(p.match(']'))
		}
		if p.env.Get(id) == nil {
			p.env.Put(id, sym)
		} else {
			p.appendLineToConsole("Error: Variable"+id+"is already definited in the scope", true)
		}
	} else if p.token.GetTag() == lexer.LITERAL {
		param.WriteString(p.match(lexer.LITERAL))
	}
	return param.String()
}

func (p *Parser) callParams() string {
	var callParams strings.Builder
	_, isWord := p.token.(*lexer.WordToken)
	if p.token.GetTag() == '(' || p.token.GetTag() == lexer.NUM || isWord || p.token.GetTag() == lexer.LITERAL {
		callParams.WriteString(p.expr())
		for {
			if p.token.GetTag() == ',' {
				callParams.WriteString(p.match(','))
				callParams.WriteString(p.expr())
			} else {
				break
			}
		}
	}
	return callParams.String()
}

func (p *Parser) statement() string {
	tempSpace := p.space
	var statement strings.Builder

	if p.token.GetTag() == lexer.IF {
		statement.WriteString(p.match(lexer.IF))
		p.space += "\t"
		statement.WriteString("(")
		statement.WriteString(p.booleanExpr())
		p.match(lexer.LBRACE)
		statement.WriteString(") {\n ")
		statement.WriteString(p.optStatement())
		p.match(lexer.RBRACE)
		p.space = tempSpace
		statement.WriteString(p.space)
		statement.WriteString("}")
	} else if p.token.GetTag() == lexer.WHILE {
		statement.WriteString(p.match(lexer.WHILE))
		p.space += "\t"
		statement.WriteString("(")
		statement.WriteString(p.booleanExpr())
		statement.WriteString(") {\n ")
		p.match(lexer.LBRACE)
		statement.WriteString(p.optStatement())
		p.match(lexer.RBRACE)
		p.space = tempSpace
		statement.WriteString(p.space)
		statement.WriteString("}")
	} else if p.token.GetTag() == lexer.REPEAT {
		p.match(lexer.REPEAT)
		p.space += "\t"
		statement.WriteString("for (int i = 0; i < ")
		statement.WriteString(p.expr())
		statement.WriteString("; i++) {\n")
		p.match(lexer.LBRACE)
		statement.WriteString(p.optStatement())
		p.match(lexer.RBRACE)
		p.space = tempSpace
		statement.WriteString(p.space)
		statement.WriteString("}")
	} else if p.token.GetTag() == lexer.ID {
		id := p.match(lexer.ID)
		statement.WriteString(id)
		if p.token.GetTag() == '(' {
			statement.WriteString(p.match('('))
			statement.WriteString(p.callParams())
			statement.WriteString(p.match(')'))
			statement.WriteString(p.match(';'))
		} else {
			if p.token.GetTag() == '[' {
				statement.WriteString(p.match('['))
				statement.WriteString(p.expr())
				statement.WriteString(p.match(']'))
			}
			statement.WriteString(p.match(lexer.ASSIGN))
			value := p.expr()
			statement.WriteString(value)
			sym := p.env.Get(id)
			if sym != nil {
				sym.Value = value
			} else {
				p.appendLineToConsole(fmt.Sprintf("Error at line: %d Cannot resolve symbol %s", p.lexer.Line, id), true)
			}
			statement.WriteString(";")
		}
	} else if p.token.GetTag() == lexer.PRINT {
		p.match(lexer.PRINT)
		statement.WriteString("Serial.print")
		statement.WriteString("(")
		statement.WriteString(p.expr())
		statement.WriteString(")")
		statement.WriteString(";")
	} else {
		p.appendLineToConsole("line: "+strconv.Itoa(p.lexer.Line)+" Not A Statement", true)
		p.nextToken()
	}
	p.hasError = false

	return p.space + statement.String()
}

func (p *Parser) booleanExpr() string {
	var booleanExpr strings.Builder
	booleanExpr.WriteString(p.expr())
	if p.token.GetTag() == lexer.COMPARATOR {
		booleanExpr.WriteString(TranslateLexeme(p.match(lexer.COMPARATOR)))
		booleanExpr.WriteString(p.expr())
	}
	return booleanExpr.String()
}

func (p *Parser) expr() string {
	var expr strings.Builder
	expr.WriteString(p.term())
	for {
		if p.token.GetTag() == '+' {
			expr.WriteString(p.match('+'))
			expr.WriteString(p.term())
			if !p.hasError {
				p.bufBuilder.WriteString("+ ")
			}
		} else if p.token.GetTag() == '-' {
			expr.WriteString(p.match('-'))
			expr.WriteString(p.term())
			if !p.hasError {
				p.bufBuilder.WriteString("- ")
			}
		} else {
			return expr.String()
		}
	}
}

func (p *Parser) term() string {
	var term strings.Builder
	term.WriteString(p.factor())
	for {
		if p.token.GetTag() == '*' {
			term.WriteString(p.match('*'))
			term.WriteString(p.factor())
			if !p.hasError {
				p.bufBuilder.WriteString("* ")
			}
		} else if p.token.GetTag() == '/' {
			term.WriteString(p.match('/'))
			term.WriteString(p.factor())
			if !p.hasError {
				p.bufBuilder.WriteString("/ ")
			}
		} else {
			return term.String()
		}
	}
}

func (p *Parser) factor() string {
	var factor strings.Builder
	if p.token.GetTag() == '(' {
		factor.WriteString(p.match('('))
		factor.WriteString(p.expr())
		factor.WriteString(p.match(')'))
	} else if num, ok := p.token.(*lexer.NumToken); ok {
		p.bufBuilder.WriteString(strconv.Itoa(num.Value))
		p.bufBuilder.WriteString(" ")
		factor.WriteString(p.match(lexer.NUM))
	} else if p.token.GetTag() == lexer.ID {
		word := p.token.(*lexer.WordToken)
		p.bufBuilder.WriteString(word.Lexeme)
		p.bufBuilder.WriteString(" ")
		id := p.match(lexer.ID)
		factor.WriteString(id)
		if p.token.GetTag() == '.' {
			factor.WriteString(p.match('.'))
			factor.WriteString(p.match(lexer.ID))
		}
		if p.token.GetTag() == '[' {
			factor.WriteString(p.match('['))
			factor.WriteString(p.expr())
			factor.WriteString(p.match(']'))
		}
		if p.env.Get(id) == nil {
			p.appendLineToConsole(fmt.Sprintf("Error at line: %d Cannot resolve symbol %s", p.lexer.Line, id), true)
		}
	} else if p.token.GetTag() == lexer.LITERAL {
		factor.WriteString(p.match(lexer.LITERAL))
	} else {
		p.error(lexer.NUM, "NUM/Word/LITERAL")
	}
	return factor.String()
}

func (p *Parser) match(matchTag int) string {
	if p.token.GetTag() == matchTag {
		temp := p.token
		p.nextToken()
		return temp.Stringify()
	} else {
		p.error(matchTag, strconv.Itoa(matchTag))
		return ""
	}
}

// tag not used
func (p *Parser) error(_ int, expected string) {
	if !p.hasError {
		p.hasError = true
		last := bytes.LastIndex(p.bufBuilder.Bytes(), []byte("\n"))
		if last >= 0 {
			p.bufBuilder.Truncate(last)
			p.appendLineToConsole("ExpectedError at line: "+strconv.Itoa(p.lexer.Line)+""+expected, true)
			p.skipErrors(&lexer.BaseToken{})
		}
	}
}

func (p *Parser) skipErrors(expected lexer.Token) {
	if p.isEndToken(expected) && p.isEndToken(p.token) {
		for {
			p.nextToken()
			if !p.isEndToken(p.token) {
				break
			}
		}
	}
}

func (p *Parser) nextToken() {
	for {
		t, err := p.lexer.Tokenize()
		if err != nil {
			p.token = &lexer.BaseToken{Tag: lexer.EOF}
			return
		}
		p.token = t
		if p.token.GetTag() != lexer.COMMENT {
			break
		}
	}
	p.appendLineToConsole(p.token.String(), false)
}

func (p *Parser) isEndToken(token lexer.Token) bool {
	return token.GetTag() != ';' &&
		token.GetTag() != lexer.EOF &&
		token.GetTag() != lexer.RBRACE &&
		token.GetTag() != lexer.RETURN
}

func (p *Parser) appendLineToConsole(str string, isError bool) {
	p.console.WriteString(str)
	p.console.WriteString("\n")
	if isError {
		p.errors = append(p.errors, p.line)
	}
	p.line++
}
