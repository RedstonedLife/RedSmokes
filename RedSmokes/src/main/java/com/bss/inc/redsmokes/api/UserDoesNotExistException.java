package com.bss.inc.redsmokes.api;


import static com.bss.inc.redsmokes.main.I18n.tl;

/**
 * @deprecated This is unused - see {@link com.bss.inc.redsmokes.api}.
 */
@Deprecated
public class UserDoesNotExistException extends Exception {
    public UserDoesNotExistException(final String name) {
        super(tl("userDoesNotExist", name));
    }
}
