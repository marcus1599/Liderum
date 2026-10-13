ALTER TABLE users ALTER COLUMN guild_id SET NOT NULL;
ALTER TABLE users ALTER COLUMN guild_role SET NOT NULL;

ALTER TABLE event ALTER COLUMN name SET NOT NULL;
ALTER TABLE event ALTER COLUMN date SET NOT NULL;

ALTER TABLE attendance ALTER COLUMN status SET NOT NULL;
ALTER TABLE attendance
    ADD CONSTRAINT uk_attendance_member_event UNIQUE (member_id, event_id);
