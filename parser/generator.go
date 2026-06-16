package parser

func TranslateLexeme(lexeme string) string {
	switch lexeme {
	case "Int":
		return "int "
	case "Char":
		return "char "
	case "Boolean":
		return "boolean "
	case "<>":
		return "!= "
	default:
		return lexeme + " "
	}
}
