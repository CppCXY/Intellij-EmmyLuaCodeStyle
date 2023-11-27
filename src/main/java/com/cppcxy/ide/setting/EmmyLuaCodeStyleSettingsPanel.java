package com.cppcxy.ide.setting;

import com.cppcxy.intellij.lua.settings.EmmyLuaCodeStyleSettings;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class EmmyLuaCodeStyleSettingsPanel implements SearchableConfigurable, Configurable.NoScroll {
    private JPanel myPanel;
    private JCheckBox codeStyleCheckCheckBox;
    private JCheckBox nameStyleCheckCheckBox;
    private JTextField globalConfigPath;

    private final EmmyLuaCodeStyleSettings settings = EmmyLuaCodeStyleSettings.getInstance();

    public EmmyLuaCodeStyleSettingsPanel() {
        codeStyleCheckCheckBox.setSelected(settings.getCodeStyleCheck());

        nameStyleCheckCheckBox.setSelected(settings.getNameStyleCheck());

        globalConfigPath.setText(settings.getGlobalConfigPath());
    }


    @Override
    public @NotNull @NonNls String getId() {
        return "EmmyLuaCodeStyleSettingsPanel";
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "EmmyLuaCodeStyle Settings";
    }

    @Override
    public @Nullable JComponent createComponent() {
        return myPanel;
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        settings.setCodeStyleCheck(codeStyleCheckCheckBox.isSelected());
        settings.setNameStyleCheck(nameStyleCheckCheckBox.isSelected());
        settings.setGlobalConfigPath(globalConfigPath.getText());
    }
}
