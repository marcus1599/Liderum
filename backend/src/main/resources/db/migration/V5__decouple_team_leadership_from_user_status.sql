ALTER TABLE member
    ADD CONSTRAINT uk_member_team_guild_id UNIQUE (team_id, guild_id, id);

ALTER TABLE team
    ADD COLUMN leader_member_id BIGINT;

UPDATE team t
SET leader_member_id = (
    SELECT m.id
    FROM users u
    JOIN member m ON m.id = u.member_id
    WHERE u.id = t.leader_id
      AND m.team_id = t.id
      AND m.guild_id = t.guild_id
)
WHERE EXISTS (
    SELECT 1
    FROM users u
    JOIN member m ON m.id = u.member_id
    WHERE u.id = t.leader_id
      AND m.team_id = t.id
      AND m.guild_id = t.guild_id
);

ALTER TABLE team
    ADD CONSTRAINT fk_team_leader_member_same_team
    FOREIGN KEY (id, guild_id, leader_member_id)
    REFERENCES member (team_id, guild_id, id);

ALTER TABLE team
    DROP CONSTRAINT fk_team_leader;

CREATE INDEX idx_team_leader_member ON team (leader_member_id);
