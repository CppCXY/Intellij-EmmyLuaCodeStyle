package com.cppcxy.intellij.lua.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
@State(name = "EmmyLuaCodeStyleSettings", storages = [Storage("emmyluacodestyle.xml")])
class EmmyLuaCodeStyleSettings : PersistentStateComponent<EmmyLuaCodeStyleSettings>{
    var codeStyleCheck = true
    var nameStyleCheck = false
    var globalConfigPath = ""

    companion object {
        @JvmStatic
        fun getInstance(): EmmyLuaCodeStyleSettings {
            return ApplicationManager.getApplication().getService(EmmyLuaCodeStyleSettings::class.java)
        }
    }

    override fun getState(): EmmyLuaCodeStyleSettings {
        return this
    }

    override fun loadState(state: EmmyLuaCodeStyleSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
