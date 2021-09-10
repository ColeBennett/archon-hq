package net.thearchon.hq.service.votifier;

import net.thearchon.hq.service.Listener;

public interface VoteListener extends Listener {

	/**
	 * Notified when a vote has been received from a voting service.
	 * @param vote the vote that has been made
	 */
	void voteMade(Vote vote);
}
