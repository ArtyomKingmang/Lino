package main

import (
	"flag"
	"fmt"
	"os"

	"github.com/kingmang/lino/lexer"
	"github.com/kingmang/lino/parser"
)

func main() {
	inputFile := flag.String("input", "input.lino", "Input .lino file")
	outputFile := flag.String("output", "output.ino", "Output .ino file")
	flag.Parse()

	file, err := os.Open(*inputFile)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error opening input file: %v\n", err)
		os.Exit(1)
	}
	defer file.Close()

	l := lexer.NewLexer(file)
	p := parser.NewParser(l)

	err = p.Program()
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error during parsing: %v\n", err)
		os.Exit(1)
	}

	err = os.WriteFile(*outputFile, []byte(p.Output()), 0644)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error writing output file: %v\n", err)
		os.Exit(1)
	}

	//fmt.Print(p.Console())
}
