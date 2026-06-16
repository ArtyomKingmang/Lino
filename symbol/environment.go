package symbol

type Environment struct {
	table    map[string]*Symbol
	previous *Environment
}

func NewEnvironment(previous *Environment) *Environment {
	return &Environment{
		table:    make(map[string]*Symbol),
		previous: previous,
	}
}

func (e *Environment) Put(name string, sym *Symbol) {
	e.table[name] = sym
}

func (e *Environment) Get(name string) *Symbol {
	for env := e; env != nil; env = env.previous {
		if found, ok := env.table[name]; ok {
			return found
		}
	}
	return nil
}
