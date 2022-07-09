package com.bss.inc.redsmokes.main.updatecheck;

import com.bss.inc.redsmokes.main.RedSmokes;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
    private CompletableFuture<RemoteVersion> pendingReleaseFuture;
    private String latestRelease = null;
    private RemoteVersion cachedDev = null;
    private RemoteVersion cachedRelease = null;

    public UpdateChecker(RedSmokes redSmokes) {
        String identifier = "INVALID";
        String branch = "INVALID";
        boolean dev = false;

        final InputStream inputStream = UpdateChecker.class.getClassLoader().getResourceAsStream("release");
        if (inputStream != null) {
            final List<String> versionStr = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.toList());
            if (versionStr.size() == 2) {
                if (versionStr.get(0).matches("\\d+\\.\\d+\\.\\d+-(?:dev|rc|beta|alpha)\\+\\d+-[0-9a-f]{7,40}")) {
                    identifier = versionStr.get(0).split("-")[2];
                    dev = true;
                } else {
                    identifier = versionStr.get(0);
                }
                branch = versionStr.get(1);
            }
        }

        this.redSmokes = redSmokes;
        this.versionIdentifier = identifier;
        this.versionBranch = branch;
        this.devBuild = dev;
    }
}
