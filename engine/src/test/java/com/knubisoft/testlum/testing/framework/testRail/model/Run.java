package com.knubisoft.testlum.testing.framework.testRail.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
public class Run {
	private Integer id;
	private Integer projectId;
	private String name;
	private String description;
	private String config;
	private Integer passedCount;
	private Integer failedCount;
	private Integer untestedCount;
	private Integer retestCount;
	private String url;
	private Long createdOn;
	private Long updatedOn;
	private Long completedOn;
	private Boolean isCompleted;
	private Boolean includeAll;
	private List<Integer> caseIds;
}
