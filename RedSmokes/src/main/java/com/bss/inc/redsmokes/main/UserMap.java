package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.api.IConf;
import com.google.common.cache.CacheLoader;

public class UserMap extends CacheLoader<String, User> implements IConf {
}
