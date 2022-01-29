/
CREATE FUNCTION `LS_ALLOC_SPACE`(`REQ_MEDIA_TYPE` VARCHAR(128),`REQ_CONTENT_LENGTH` BIGINT(20)) RETURNS VARCHAR(16)
BEGIN
	DECLARE `HISTORY_MARKER` VARCHAR(36) DEFAULT UUID();
	DECLARE `RS_STORAGE_NAME` VARCHAR(16) DEFAULT NULL;
	INSERT INTO `SPACE_ALLOCATIONS_HISTORY` SELECT
		`HISTORY_MARKER` AS `GUID`,
		`S`.`STORAGE_NAME` AS `STORAGE_NAME`,
		'ALLOC' AS `OPERATION_TYPE`,
		`REQ_CONTENT_LENGTH` AS `SIZE`
	FROM (SELECT
			`S`.`STORAGE_NAME`,
			`LS`.`DISK_QUOTE` - SUM(CASE
				WHEN `H`.`OPERATION_TYPE` = 'ALLOC' THEN `H`.`SIZE`
				ELSE 0
			END) + SUM(CASE
				WHEN `H`.`OPERATION_TYPE` = 'RELEASE' THEN `H`.`SIZE`
				ELSE 0
			END) AS `AVAILABLE_SIZE`
		FROM
			`REF_LOCAL_STORAGES` `LS`
		LEFT JOIN `REF_STORAGES` `S` ON
			`LS`.`STORAGE_NAME` = `S`.`STORAGE_NAME`
		LEFT JOIN `SPACE_ALLOCATIONS_HISTORY` `H` ON
			`H`.`STORAGE_NAME` = `LS`.`STORAGE_NAME`
		WHERE
			`S`.`MEDIA_TYPE` = `REQ_MEDIA_TYPE`
		GROUP BY
			`S`.`STORAGE_NAME`
		HAVING
			`AVAILABLE_SIZE` >= `REQ_CONTENT_LENGTH`
		ORDER BY
			`AVAILABLE_SIZE` ASC
		LIMIT 1) `S`;
	SELECT 
		`H`.`STORAGE_NAME` INTO `RS_STORAGE_NAME` 
	FROM `SPACE_ALLOCATIONS_HISTORY` `H` 
	WHERE `H`.`GUID` = `HISTORY_MARKER`;
	RETURN (`RS_STORAGE_NAME`);
END
/
CREATE FUNCTION `LS_RELEASE_SPACE`(`REQ_STORAGE_NAME` VARCHAR(16), `REQ_CONTENT_LENGTH` BIGINT(20)) RETURNS VARCHAR(36)
BEGIN
	DECLARE `HISTORY_MARKER` VARCHAR(36) DEFAULT UUID();
	INSERT INTO `SPACE_ALLOCATIONS_HISTORY`(`GUID`, `STORAGE_NAME`, `OPERATION_TYPE`, `SIZE`)
	VALUES (`HISTORY_MARKER`, `REQ_STORAGE_NAME`, 'RELEASE', `REQ_CONTENT_LENGTH`);
	RETURN (`HISTORY_MARKER`);
END
/
