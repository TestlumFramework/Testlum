package com.knubisoft.testlum.testing.framework.testRail.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
public class Suite {
	private Integer id;
	private String name;
	private String description;
	private Integer projectId;
	private String url;
}
