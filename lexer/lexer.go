package lexer

import (
	"bufio"
	"io"
	"unicode"
)

type Lexer struct {
	reader   *bufio.Reader
	peek     rune
	Line     int
	keywords map[string]*WordToken
}

func NewLexer(reader io.Reader) *Lexer {
	l := &Lexer{
		reader:   bufio.NewReader(reader),
		Line:     1,
		peek:     ' ',
		keywords: make(map[string]*WordToken),
	}
	l.putKeyword(NewWordToken(LITERAL, "true"))
	l.putKeyword(NewWordToken(LITERAL, "false"))
	l.putKeyword(NewWordToken(FUNC, "func"))
	l.putKeyword(NewWordToken(TYPE, "Int"))
	l.putKeyword(NewWordToken(TYPE, "Char"))
	l.putKeyword(NewWordToken(TYPE, "String"))
	l.putKeyword(NewWordToken(TYPE, "Boolean"))
	l.putKeyword(NewWordToken(RETURN, "return"))
	l.putKeyword(NewWordToken(WHILE, "while"))
	l.putKeyword(NewWordToken(IF, "if"))
	l.putKeyword(NewWordToken(REPEAT, "repeat"))
	l.putKeyword(NewWordToken(VOID, "void"))
	l.putKeyword(NewWordToken(PRINT, "print"))
	return l
}

func (l *Lexer) putKeyword(t *WordToken) {
	l.keywords[t.Lexeme] = t
}

func (l *Lexer) nextChar() (rune, error) {
	r, _, err := l.reader.ReadRune()
	return r, err
}

func (l *Lexer) Tokenize() (Token, error) {
	for {
		if l.peek == ' ' || l.peek == '\t' || l.peek == '\r' {
			ch, err := l.nextChar()
			if err != nil {
				if err == io.EOF {
					l.peek = 0
					return &BaseToken{EOF}, nil
				}
				return nil, err
			}
			l.peek = ch
			continue
		} else if l.peek == '\n' {
			l.Line++
			ch, err := l.nextChar()
			if err != nil {
				if err == io.EOF {
					l.peek = 0
					return &BaseToken{EOF}, nil
				}
				return nil, err
			}
			l.peek = ch
			continue
		} else {
			break
		}
	}

	if l.peek == '/' {
		ch, err := l.nextChar()
		if err != nil {
			if err == io.EOF {
				l.peek = 0
				return &BaseToken{'/'}, nil
			}
			return nil, err
		}
		l.peek = ch

		switch l.peek {
		case '/':
			for {
				ch, err := l.nextChar()
				if err != nil {
					if err == io.EOF {
						l.peek = 0
						return &BaseToken{COMMENT}, nil
					}
					return nil, err
				}
				l.peek = ch
				if l.peek == '\n' || l.peek == 0 {
					l.peek = ' '
					l.Line++
					return &BaseToken{COMMENT}, nil
				}
			}
		case '*':
			ch, err := l.nextChar()
			if err != nil {
				if err == io.EOF {
					l.peek = 0
					return &BaseToken{COMMENT}, nil
				}
				return nil, err
			}
			l.peek = ch

			for {
				switch l.peek {
				case '\n':
					l.Line++
				case '*':
					ch, err := l.nextChar()
					if err != nil {
						if err == io.EOF {
							l.peek = 0
							return &BaseToken{COMMENT}, nil
						}
						return nil, err
					}
					l.peek = ch
					if l.peek == '/' {
						ch, err := l.nextChar()
						if err != nil {
							if err == io.EOF {
								l.peek = 0
								return &BaseToken{COMMENT}, nil
							}
							return nil, err
						}
						l.peek = ch
						return &BaseToken{COMMENT}, nil
					}
				}
				if l.peek == 0 {
					return &BaseToken{COMMENT}, nil
				}
				ch, err := l.nextChar()
				if err != nil {
					if err == io.EOF {
						l.peek = 0
						return &BaseToken{COMMENT}, nil
					}
					return nil, err
				}
				l.peek = ch
			}
		default:
			t := &BaseToken{'/'}
			l.peek = ' '
			return t, nil
		}
	}

	if unicode.IsDigit(l.peek) {
		result := 0
		for unicode.IsDigit(l.peek) {
			result = 10*result + int(l.peek-'0')
			ch, err := l.nextChar()
			if err != nil {
				if err == io.EOF {
					l.peek = 0
					return NewNumToken(result), nil
				}
				return nil, err
			}
			l.peek = ch
		}
		return NewNumToken(result), nil
	}

	if unicode.IsLetter(l.peek) {
		builder := []rune{}
		for unicode.IsLetter(l.peek) || unicode.IsDigit(l.peek) {
			builder = append(builder, l.peek)
			ch, err := l.nextChar()
			if err != nil {
				if err == io.EOF {
					l.peek = 0
					break
				}
				return nil, err
			}
			l.peek = ch
		}
		buffer := string(builder)
		if word, ok := l.keywords[buffer]; ok {
			return word, nil
		}
		word := NewWordToken(ID, buffer)
		l.keywords[buffer] = word
		return word, nil
	}

	if l.peek == ':' {
		ch, err := l.nextChar()
		if err != nil {
			if err == io.EOF {
				l.peek = 0
				return &BaseToken{COLON}, nil
			}
			return nil, err
		}
		l.peek = ch
		return &BaseToken{COLON}, nil
	}

	if l.peek == '<' {
		ch, err := l.nextChar()
		if err != nil {
			if err == io.EOF {
				l.peek = 0
				return NewOperatorToken("<"), nil
			}
			return nil, err
		}
		l.peek = ch

		switch l.peek {
		case '>':
			ch, err := l.nextChar()
			if err != nil {
				if err == io.EOF {
					l.peek = 0
					return NewOperatorToken("<>"), nil
				}
				return nil, err
			}
			l.peek = ch
			return NewOperatorToken("<>"), nil
		case '=':
			ch, err := l.nextChar()
			if err != nil {
				if err == io.EOF {
					l.peek = 0
					return NewOperatorToken("<="), nil
				}
				return nil, err
			}
			l.peek = ch
			return NewOperatorToken("<="), nil
		default:
			return NewOperatorToken("<"), nil
		}
	}

	if l.peek == '>' {
		ch, err := l.nextChar()
		if err != nil {
			if err == io.EOF {
				l.peek = 0
				return NewOperatorToken(">"), nil
			}
			return nil, err
		}
		l.peek = ch

		if l.peek == '=' {
			ch, err := l.nextChar()
			if err != nil {
				if err == io.EOF {
					l.peek = 0
					return NewOperatorToken(">="), nil
				}
				return nil, err
			}
			l.peek = ch
			return NewOperatorToken(">="), nil
		} else {
			return NewOperatorToken(">"), nil
		}
	}

	if l.peek == '=' {
		ch, err := l.nextChar()
		if err != nil {
			if err == io.EOF {
				l.peek = 0
				return &BaseToken{ASSIGN}, nil
			}
			return nil, err
		}
		l.peek = ch

		if l.peek == '=' {
			ch, err := l.nextChar()
			if err != nil {
				if err == io.EOF {
					l.peek = 0
					return NewOperatorToken("=="), nil
				}
				return nil, err
			}
			l.peek = ch
			return NewOperatorToken("=="), nil
		} else {
			ch, err := l.nextChar()
			if err != nil {
				if err == io.EOF {
					l.peek = 0
					return &BaseToken{ASSIGN}, nil
				}
				return nil, err
			}
			l.peek = ch
			return &BaseToken{ASSIGN}, nil
		}
	}

	if l.peek == '}' {
		ch, err := l.nextChar()
		if err != nil {
			if err == io.EOF {
				l.peek = 0
				return &BaseToken{RBRACE}, nil
			}
			return nil, err
		}
		l.peek = ch
		return &BaseToken{RBRACE}, nil
	}

	if l.peek == '{' {
		ch, err := l.nextChar()
		if err != nil {
			if err == io.EOF {
				l.peek = 0
				return &BaseToken{LBRACE}, nil
			}
			return nil, err
		}
		l.peek = ch
		return &BaseToken{LBRACE}, nil
	}

	if l.peek == '"' {
		builder := []rune{}
		ch, err := l.nextChar()
		if err != nil {
			if err == io.EOF {
				l.peek = 0
				return NewLiteralToken(string(builder)), nil
			}
			return nil, err
		}
		l.peek = ch

		for l.peek != '"' && l.peek != '\n' && l.peek != 0 {
			builder = append(builder, l.peek)
			ch, err := l.nextChar()
			if err != nil {
				if err == io.EOF {
					l.peek = 0
					return NewLiteralToken(string(builder)), nil
				}
				return nil, err
			}
			l.peek = ch
		}
		ch, err = l.nextChar()
		if err != nil {
			if err == io.EOF {
				l.peek = 0
				return NewLiteralToken(string(builder)), nil
			}
			return nil, err
		}
		l.peek = ch
		return NewLiteralToken(string(builder)), nil
	}

	t := &BaseToken{int(l.peek)}
	l.peek = ' '
	return t, nil
}
