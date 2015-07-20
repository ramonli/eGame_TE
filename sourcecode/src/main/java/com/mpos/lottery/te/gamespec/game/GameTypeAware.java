package com.mpos.lottery.te.gamespec.game;

/**
 * For some cases, a implementation completely depends on game type.
 * 
 * @author Ramon
 * 
 */
public interface GameTypeAware {

    /**
     * Which game type does the implementation supports?
     * 
     * @return the support game type, refer to Game.TYPE_XXX
     */
    GameType supportedGameType();
}
