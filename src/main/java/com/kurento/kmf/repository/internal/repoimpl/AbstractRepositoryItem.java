package com.kurento.kmf.repository.internal.repoimpl;

import java.util.Map;

import com.kurento.kmf.repository.RepositoryHttpPlayer;
import com.kurento.kmf.repository.RepositoryHttpRecorder;
import com.kurento.kmf.repository.RepositoryItem;
import com.kurento.kmf.repository.RepositoryItemAttributes;

public abstract class AbstractRepositoryItem implements RepositoryItem {

	protected RepositoryWithHttp repository;
	protected String id;
	protected volatile State state;
	protected RepositoryItemAttributes attributes;
	protected Map<String, String> metadata;

	public AbstractRepositoryItem(String id, State state,
			RepositoryItemAttributes attributes, RepositoryWithHttp repository) {
		this.repository = repository;
		this.id = id;
		this.state = state;
		this.attributes = attributes;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public State getState() {
		return state;
	}

	protected void checkState(State desiredState) {
		if (state != desiredState) {
			throw new IllegalStateException("The item is in state \"" + state
					+ "\" but is should be in state \"" + desiredState + "\"");
		}
	}

	@Override
	public RepositoryHttpPlayer createRepositoryHttpPlayer() {
		return repository.getRepositoryHttpManager()
				.createRepositoryHttpPlayer(this);
	}

	@Override
	public RepositoryHttpRecorder createRepositoryHttpRecorder() {
		return repository.getRepositoryHttpManager()
				.createRepositoryHttpRecorder(this);
	}

	@Override
	public RepositoryHttpPlayer createRepositoryHttpPlayer(String sessionIdInURL) {
		return repository.getRepositoryHttpManager()
				.createRepositoryHttpPlayer(this, sessionIdInURL);
	}

	@Override
	public RepositoryHttpRecorder createRepositoryHttpRecorder(
			String sessionIdInURL) {
		return repository.getRepositoryHttpManager()
				.createRepositoryHttpRecorder(this, sessionIdInURL);
	}

	@Override
	public RepositoryItemAttributes getAttributes() {
		return attributes;
	}

	@Override
	public Map<String, String> getMetadata() {
		return metadata;
	}

	@Override
	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	@Override
	public void putMetadataEntry(String key, String value) {
		this.metadata.put(key, value);
	}

}