/*
 * Copyright (c) 2013 Tah Wei Hoon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License Version 2.0,
 * with full text available at http://www.apache.org/licenses/LICENSE-2.0.html
 *
 * This software is provided "as is". Use at your own risk.
 */
package com.dzyuba.javaboost.presentation.ide.common.utils;

/**
 * Singleton class that represents a non-programming language without keywords,
 * operators etc.
 */
public class LanguageNonPro extends Language {
    private static Language _theOne = null;

    private final static String[] keywords = {};


    public static Language getInstance() {
        if (_theOne == null) {
            _theOne = new LanguageNonPro();
        }
        return _theOne;
    }

    private LanguageNonPro() {
        super.setKeywords(keywords);
    }

    @Override
    public boolean isProLang() {
        return false;
    }

}
