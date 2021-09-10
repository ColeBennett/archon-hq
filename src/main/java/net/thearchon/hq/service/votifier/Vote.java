package net.thearchon.hq.service.votifier;

public class Vote {

	private final String serviceName;
	private final String username;
	private final String address;
	private final String timeStamp;

	Vote(String serviceName, String username, String address, String timeStamp) {
		this.serviceName = serviceName;
		this.username = username;
		this.address = address;
		this.timeStamp = timeStamp;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getUsername() {
		return username;
	}

	public String getAddress() {
		return address;
	}

	public String getTimeStamp() {
		return timeStamp;
	}
}
