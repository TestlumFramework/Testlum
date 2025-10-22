package com.knubisoft.testlum.testing.framework.testRail.model;


import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
public class Project {
	private Integer id;
	private String name;
	private String announcement;
	private Integer suiteMode;
	private String url;
}
