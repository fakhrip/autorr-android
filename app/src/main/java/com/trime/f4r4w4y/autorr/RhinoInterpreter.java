package com.trime.f4r4w4y.autorr;

import android.util.Log;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

public class RhinoInterpreter {
    private Context rhino;
    private Scriptable scope;

    public RhinoInterpreter() {
        initialize();
    }

    public void initialize() {
        rhino = Context.enter();
        rhino.setLanguageVersion(Context.VERSION_ES6);
        rhino.setOptimizationLevel(-1);

        scope = rhino.initStandardObjects();
        rhino.getWrapFactory().setJavaPrimitiveWrap(false);
    }

    public void loadLib(String jscode) {
        evalScript(jscode, "javascriptLibrary");
    }

    public void evalScript(String jscode, String scriptName) {
        try {
            Object result = rhino.evaluateString(scope, jscode, scriptName, 1, null);
            Log.d("JS_RESULT", result.toString());
        } catch (org.mozilla.javascript.EvaluatorException e) {
            Log.e("JS_ERROR_EVALUATOR", e.getMessage());
        } catch (org.mozilla.javascript.EcmaError e) {
            Log.e("JS_ERROR_ECMA", e.getMessage());
        } catch (Exception e) {
            Log.e("JS_ERROR", e.getMessage());
        }
    }

    public Object getJsFunction(String name) {
        return scope.get(name, scope);
    }

    @SuppressWarnings("unchecked")
    public <T> T callJsFunction(String name, Object... params) {
        Object obj = getJsFunction(name);
        if (obj instanceof Function) {
            Function function = (Function) obj;
            return (T) function.call(rhino, scope, scope, params);
        }

        return null;
    }
}
