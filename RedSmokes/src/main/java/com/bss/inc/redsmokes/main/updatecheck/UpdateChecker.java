package com.bss.inc.redsmokes.main.updatecheck;

import com.bss.inc.redsmokes.main.RedSmokes;

import java.util.concurrent.CompletableFuture;

public class UpdateChecker {
    private static final String REPO = "RedSmokes/RedSmokes";
    private static final String BRANCH = "main";

    private static final String LATEST_RELEASE_URL = "https://api.github.com/repos/"+REPO+"/releases/latest";
    // 0 = base for comparison, 1 = head for comparison - *not* the same as what this class calls them
    private static final String DISTANCE_URL = "https://api.github.com/repos/RedSmokes/RedSmokes/compare/{0}...{1}";

    private final RedSmokes redSmokes;
    private final String versionIdentifier;
    private final String versionBranch;
    private final boolean devBuild;

    private long lastFetchTime = 0;
    private CompletableFuture<RemoteVersion> pendingDevFuture;
    private CompletableFuture<RemoteVersion>
}
