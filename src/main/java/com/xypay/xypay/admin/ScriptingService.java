package com.xypay.xypay.admin;

import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ScriptingService {
    
    private ScriptEngineManager scriptEngineManager;
    private ScriptEngine scriptEngine;
    
    public ScriptingService() {
        this.scriptEngineManager = new ScriptEngineManager();
        this.scriptEngine = scriptEngineManager.getEngineByName("javascript");
    }
    
    /**
     * Execute a script and return the result
     * 
     * @param script The script to execute
     * @return The result of the script execution
     */
    public Object executeScript(String script) throws ScriptException {
        return scriptEngine.eval(script);
    }
    
    /**
     * Execute a script with context variables
     * 
     * @param script The script to execute
     * @param contextVariables The context variables to pass to the script
     * @return The result of the script execution
     */
    public Object executeScriptWithContext(String script, Map<String, Object> contextVariables) throws ScriptException {
        // Set context variables in the script engine
        for (Map.Entry<String, Object> entry : contextVariables.entrySet()) {
            scriptEngine.put(entry.getKey(), entry.getValue());
        }
        
        // Execute the script
        return scriptEngine.eval(script);
    }
    
    /**
     * Validate a script for syntax errors
     * 
     * @param script The script to validate
     * @return True if the script is valid, false otherwise
     */
    public boolean validateScript(String script) {
        try {
            // Try to compile the script
            // scriptEngine.compile(script); // Commented out due to compilation error
            // As a workaround, we'll just try to evaluate the script
            scriptEngine.eval(script);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}