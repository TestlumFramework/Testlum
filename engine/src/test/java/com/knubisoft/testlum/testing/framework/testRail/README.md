# TestRail Integration Documentation

This document provides a detailed guide on how to set up and use TestRail integration for automated reporting of test results at the end of test execution.

---

## 1️⃣ Configuration in `global-config`

To enable TestRail integration, you must add a `<testRailsApi>` section inside your `global-config` XML file.

### Example:

```xml
<testRailsApi>
    <username>your_email@example.com</username> <!-- required -->
    <apiKey>your_api_key</apiKey> <!-- required -->
    <url>https://yourdomain.testrail.io/</url> <!-- required -->
    <projectId>1</projectId> <!-- optional -->
    <defaultRunName>Auto Test Run</defaultRunName> <!-- optional -->
    <defaultRunDescription>Run generated automatically</defaultRunDescription> <!-- optional -->
</testRailsApi>
```

* **username**: Your TestRail account email.
* **apiKey**: API Key from TestRail.
* **url**: Base URL of your TestRail instance.
* **projectId**: (Optional) Used when creating a new test run.
* **defaultRunName**: (Optional) Name used for new test runs.
* **defaultRunDescription**: (Optional) Description used for new test runs.

✅ **Note:** The `projectId` and default values are **required** if you do not specify a `testRailRunId` in the scenario. Otherwise, new test runs cannot be created.

---

## 2️⃣ Scenario Configuration

For each scenario that should send results to TestRail, add the `<testRails>` section inside your `<overview>` block.

### Example 1️⃣ (Sending to an existing Test Run):

```xml
<overview>
    <description>TestRail API test</description>
    <name>TestRailApi1</name>
    <testRails enable="true" testRailRunId="1" testCaseId="1"/>
</overview>
```

* **enable**: Enables TestRail reporting for this scenario.
* **testRailRunId**: ID of the existing TestRail run.
* **testCaseId**: ID of the TestRail test case (required).

This will send the result of the test to the **existing run (ID=1)** and associate it with **test case ID=1**.

---

### Example 2️⃣ (Creating a new Test Run):

```xml
<overview>
    <description>TestRail API test 3</description>
    <name>TestRailApi3</name>
    <testRails enable="true" testCaseId="1"/>
</overview>
```

* Here, only `testCaseId` is specified.
* Since `testRailRunId` is missing, the system **automatically creates a new Test Run** using the `projectId`, `defaultRunName`, and `defaultRunDescription` from the global config.
* All test cases (collected by their `testCaseId`) are added to this newly created Test Run, and results are submitted.

---

## 3️⃣ Processing Logic

1️⃣ **Tests with `testRailRunId` set:**

* Results are sent directly to the specified run and mapped to the specified case ID.

2️⃣ **Tests without `testRailRunId`:**

* A new Test Run is created (if `projectId` is set in `testRailsApi`).
* All scenarios with `enable=true` and a valid `testCaseId` are added to the new Test Run.
* Test results are submitted to the newly created run.

---

## 4️⃣ XML Schema Updates

### `<testRailsApi>` (Global Config)

```xml
<x:complexType name="testRailsApi">
    <x:sequence>
        <x:element name="username" type="tns:nonEmptyString"/>
        <x:element name="apiKey" type="tns:nonEmptyString"/>
        <x:element name="url" type="tns:nonEmptyString"/>
        <x:element name="projectId" type="tns:nonEmptyString" minOccurs="0"/>
        <x:element name="defaultRunName" type="tns:nonEmptyString" minOccurs="0"/>
        <x:element name="defaultRunDescription" type="tns:nonEmptyString" minOccurs="0"/>
    </x:sequence>
    <x:attribute name="enable" type="x:boolean" use="optional" default="false"/>
</x:complexType>
```

### `<testRails>` (Per Scenario)

```xml
<x:complexType name="testRails">
    <x:attribute name="enable" type="x:boolean" use="optional" default="false"/>
    <x:attribute name="testRailRunId" type="x:int" use="optional"/>
    <x:attribute name="testCaseId" type="x:int" use="required"/>
</x:complexType>
```

---

## ✅ Best Practices

* Always ensure `testCaseId` is provided (it is **required**).
* Use `testRailRunId` if you want to **send results to an existing run**.
* If no `testRailRunId` is provided, make sure `projectId`, `defaultRunName`, and `defaultRunDescription` are properly configured in `global-config`.
* Check your API permissions in TestRail to ensure you can **create runs** and **send results**.

---

## 🚀 How It Works (Flow)

1️⃣ The framework checks if TestRail integration is enabled in the global config.

2️⃣ All scenarios with `<testRails enable="true" ... />` are collected.

3️⃣ Scenarios are split into two groups:

* ✅ Those with a `testRailRunId`.
* ✅ Those **without** a `testRailRunId`.

4️⃣ The system:

* Sends results to existing runs for the first group.
* Creates a new Test Run and submits results for the second group.

---

## ℹ️ Additional Info

* Errors and responses from TestRail are logged.
* The integration uses TestRail's [add\_results\_for\_cases](https://www.gurock.com/testrail/docs/api/reference/results) and [add\_run](https://www.gurock.com/testrail/docs/api/reference/runs) API endpoints.

---

Feel free to expand this documentation with troubleshooting, diagrams, or step-by-step screenshots as needed ✅.
