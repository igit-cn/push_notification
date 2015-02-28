package com.yidian.push.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Profiler {
	private static ConcurrentMap<Long, Profiler> map = new ConcurrentHashMap<Long, Profiler>();
	private Profiler father;

	private List<Profiler> sons;
	private String name;
	private long cost;

	private Profiler(Profiler father, String name) {
		this.father = father;
		this.name = name;
		init();
		Long id = Thread.currentThread().getId();
		map.put(id, this);
	}

	protected void init() {
		if (father != null) {
			father.sons.add(this);
		}
		cost = System.currentTimeMillis();
		sons = new LinkedList<Profiler>();
	}

	public static Profiler start() {
		return start(null);
	}

	public static Profiler start(String name) {
		Long id = Thread.currentThread().getId();
		Profiler p = map.get(id);
//		name = c.getName() + "." + name;
		if (name == null) {
			name = Thread.currentThread().getStackTrace()[3].toString();
		} else {
			name = Thread.currentThread().getStackTrace()[2].toString() + "-" + name;
		}
		if (p == null) {
			p = new Profiler(null, name);
		} else {
			p = new Profiler(p, name);
		}
		return p;
	}

	public long end(Object text) {
		Long id = Thread.currentThread().getId();
		Profiler p = map.get(id);
		while (p != this) {
			p.cost = System.currentTimeMillis() - p.cost;
			p.name += "(not accurate)";
			p = p.father;
		}
		p.name += '[' + text.toString() + ']';
		p.cost = System.currentTimeMillis() - p.cost;
		if (p.father == null) {
			map.remove(id);
		} else {
			map.put(id, p.father);
		}
		return p.cost;
	}

	public long end() {
		Long id = Thread.currentThread().getId();
		Profiler p = map.get(id);
		while (p != this) {
			p.cost = System.currentTimeMillis() - p.cost;
			p.name += "(not accurate)";
			p = p.father;
		}
		p.cost = System.currentTimeMillis() - p.cost;
		if (p.father == null) {
			map.remove(id);
		} else {
			map.put(id, p.father);
		}
		return p.cost;
	}

//	public static void end(Class c, String name) {
//		Long id = Thread.currentThread().getId();
//		name = c.getName() + "." + name;
//		Profiler p = map.get(id);
//		while (p != null) {
//			p.cost = System.currentTimeMillis() - p.cost;
//			if (p.father == null)
//				map.remove(id);
//			else
//				map.put(id, p.father);
//			if (p.name.equals(name)) break;
//			p.name += "(not accurate)";
//			p = p.father;
//		}
//		return p == null ? 0 : p.cost;
//	}

	public long getCost() {
		return cost;
	}

	public StringBuilder display() {
		StringBuilder sb = new StringBuilder();
		display(sb, 0);
		return sb;
	}

	private void display(StringBuilder sb, int deep) {
		for (int i = 0; i < deep; i++) {
			sb.append('\t');
		}
		sb.append(String.format("%s:%.3fs\n", name, cost / 1000.0));
		for (Profiler p : sons) {
			p.display(sb, deep+1);
		}
	}

	@Override
	public String toString() {
		return String.format("Profiler:%s", name);
	}
}
