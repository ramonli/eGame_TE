package com.mpos.lottery.te.port.domain.router;

import com.google.gson.Gson;

import com.mpos.lottery.common.router.RequestMap;
import com.mpos.lottery.common.router.RoutineStrategy;
import com.mpos.lottery.common.router.Version;
import com.mpos.lottery.te.gamespec.game.Game;

/**
 * A routine key uniquely identify a request handling path. The router will route incoming request to appreciate
 * handler.
 * <p/>
 * There are some factors to determine a routine key:
 * <ol>
 * <li>Game type - Different game type may require dedicated handler for same transaction type.</li>
 * <li>Transaction type - Each transaction type may require a dedicated handler.</li>
 * <li>Protocol version - A interface may exists multiple different versions.</li>
 * </ol>
 * 
 * @author Ramon Li
 */
public class RoutineKey implements RoutineStrategy {
    public static final int TYPE_UNDEF = Game.TYPE_UNDEF; // UNdeinfed game
                                                          // type.

    private int gameType = TYPE_UNDEF;
    private int transType;
    private Version version;

    public RoutineKey() {
    }

    /**
     * Build a <code>RoutineKey</code> instance by given parameters.
     */
    public RoutineKey(int gameType, int transType, Version version) {
        this.gameType = gameType;
        this.transType = transType;
        this.version = version;
    }

    public RoutineKey(int gameType, int transType) {
        this(gameType, transType, null);
    }

    public RoutineKey(int transType) {
        this(TYPE_UNDEF, transType, null);
    }

    /**
     * Routine key has 3 levels(from most accurate to less):
     * <ol>
     * <li>gameType+transType+version</li>
     * <li>gameTYpe+transtype</li>
     * <li>transType</li>
     * </ol>
     * For example, '4_200_2.0.0' is direct child of '4_200_', and '4_200_' is direct child of '-1_200_'. The direct
     * child of '-1_200_' will be null.
     */
    public RoutineKey child() {
        RoutineKey child = null;
        if (version != null) {
            child = new RoutineKey(this.gameType, this.transType, null);
        } else {
            if (this.gameType != TYPE_UNDEF) {
                child = new RoutineKey(this.transType);
            }
        }
        return child;
    }

    /**
     * Assemble a <code>RoutineKey</code> from a <code>Requestmap</code>. The format of value of <code>RequestMap</code>
     * must follow a json format:
     * <p/>
     * {gameType:${gameType},transType:${transType},version:${version}}
     * <p/>
     * <ol>
     * <li>gameType(int): the type identifier of a game type, it is optional.</li>
     * <li>transType(int): the request type of transaction, it is mandatory</li>
     * <li>version(string): the version of required interface, it is optional.</li>
     * </ol>
     * For example:
     * <ul>
     * <li>{gameType:1,transType:200,version:1.0}, it will be converted to RoutineKey(1_200_1.0.0)</li>
     * <li>{version:1.0,transType:200}, it will be converted to RoutineKey(-1_200_1.0.0)</li>
     * <li>{version:"1.0.1",transType:200}, it will be converted to RoutineKey(-1_200_1.0.1).</li>
     * <li>{transType:200}, it will be converted to RoutineKey(-1_200_)</li>
     * </ul>
     * the default game type is -1(undefined), and default protocol version is null.
     */
    @Override
    public void from(RequestMap requestMap) {
        this.from(requestMap.value());
    }

    /**
     * Build a <code>RoutineKey</code> from a JSON string.
     */
    public void from(String jsonStr) {
        RoutineKeyJson json = new Gson().fromJson(jsonStr, RoutineKeyJson.class);
        this.setGameType(json.getGameType() == null ? TYPE_UNDEF : json.getGameType());
        this.setTransType(json.getTransType());
        if (json.getVersion() != null) {
            this.setVersion(Version.from(json.getVersion()));
        }
    }

    private class RoutineKeyJson {
        private Integer gameType = TYPE_UNDEF;
        private int transType;
        private String version;

        public Integer getGameType() {
            return gameType;
        }

        public void setGameType(Integer gameType) {
            this.gameType = gameType;
        }

        public int getTransType() {
            return transType;
        }

        public void setTransType(int transType) {
            this.transType = transType;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    public int getGameType() {
        return gameType;
    }

    public int getTransType() {
        return transType;
    }

    public Version getVersion() {
        return version;
    }

    private void setGameType(int gameType) {
        this.gameType = gameType;
    }

    private void setTransType(int transType) {
        this.transType = transType;
    }

    private void setVersion(Version version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return new StringBuffer("").append(this.getGameType()).append("_").append(this.getTransType()).append("_")
                .append(this.getVersion() == null ? "" : this.getVersion().toString()).toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + gameType;
        result = prime * result + transType;
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RoutineKey other = (RoutineKey) obj;
        if (gameType != other.gameType) {
            return false;
        }
        if (transType != other.transType) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }
}
