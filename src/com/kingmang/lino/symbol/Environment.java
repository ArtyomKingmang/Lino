package com.kingmang.lino.symbol;

import java.util.Hashtable;

public class Environment {
    private Environment previous;
    private Hashtable table;

    public Environment(Environment previous){
        table = new Hashtable();
        this.previous = previous;
    }

    public void put(String str, Symbol symbol){
        table.put(str, symbol);
    }

    public Symbol get(String str){
        for(Environment environment = this; environment != null; environment = environment.previous){
            Symbol found = (Symbol) (environment.table.get(str));
            if(found != null) return found;
        }
        return null;
    }
}
