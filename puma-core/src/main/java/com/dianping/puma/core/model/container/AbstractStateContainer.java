package com.dianping.puma.core.model.container;

import com.dianping.puma.core.model.state.State;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractStateContainer<T extends State> implements StateContainer<T> {

	ConcurrentMap<String, T> stateMap = new ConcurrentHashMap<String, T>();

	@Override
	public T get(String name) {
		return stateMap.get(name);
	}

	@Override
	public List<T> getAll() {
		return new ArrayList<T>(stateMap.values());
	}

	@Override
	public void add(T state) {
		stateMap.put(state.getName(), state);
	}

	@Override
	public void addAll(List<T> states) {
		for (T state: states) {
			add(state);
		}
	}

	@Override
	public void remove(String name) {
		stateMap.remove(name);
	}

	@Override
	public void removeAll() {
		stateMap.clear();
	}
}
