package com.kingmang.lino.symbol;

import java.util.HashMap;
import java.util.Hashtable;

public class Environment {
    private final Environment previous;
    private final HashMap<String, Symbol> table;

    public Environment(Environment previous){
        table = new HashMap<>();
        this.previous = previous;
    }

    public void put(String str, Symbol symbol){
        table.put(str, symbol);
    }

    public Symbol get(String str){
        for(Environment environment = this; environment != null; environment = environment.previous){
            Symbol found = (environment.table.get(str));
            if(found != null) return found;
        }
        return null;
    }
}
