package com.trime.f4r4w4y.autorr

import android.util.Log
import org.mozilla.javascript.*
import org.mozilla.javascript.Function

class RhinoInterpreter {
    private lateinit var rhino: Context
    private lateinit var scope: Scriptable

    fun initialize() {
        rhino = Context.enter()
        rhino.languageVersion = Context.VERSION_ES6
        rhino.optimizationLevel = -1
        rhino.wrapFactory.isJavaPrimitiveWrap = false

        scope = rhino.initStandardObjects()
    }

    fun loadLib(jsCode: String?) {
        evalScript(jsCode, "javascriptLibrary")
    }

    fun evalScript(jsCode: String?, scriptName: String?) {
        try {
            val result = rhino.evaluateString(scope, jsCode, scriptName, 1, null)
            Log.d("JS_RESULT", result.toString())
        } catch (e: EvaluatorException) {
            Log.e("JS_ERROR_EVALUATOR", e.message!!)
        } catch (e: EcmaError) {
            Log.e("JS_ERROR_ECMA", e.message!!)
        } catch (e: Exception) {
            Log.e("JS_ERROR", e.message!!)
        }
    }

    private fun getJsFunction(name: String?): Any {
        return scope[name, scope]
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> callJsFunction(name: String, vararg params: Any?): T? {
        val obj = getJsFunction(name)
        if (obj is Function) {
            return obj.call(rhino, scope, scope, params) as T
        }
        return null
    }

    init {
        initialize()
    }
}