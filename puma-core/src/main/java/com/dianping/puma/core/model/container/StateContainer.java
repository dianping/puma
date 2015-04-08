package com.dianping.puma.core.model.container;

import com.dianping.puma.core.model.state.State;

import java.util.List;

public interface StateContainer<T extends State> {

	public T get(String name);

	public List<T> getAll();

	public void add(T state);

	public void addAll(List<T> states);

	public void remove(String name);

	public void removeAll();
}
