create table git_hub_project (
	id serial not null primary key,
	name character varying(255) not null,
	owner character varying(255) not null,
	repo character varying(255) not null
);

create table jira_project (
	id serial not null primary key,
	name character varying(255) not null,
	key character varying(255) not null,
);

create table release (
	id serial not null primary key,
	project character varying(255) not null,
	name character varying(255) not null,
	date character(10) not null
);

insert into git_hub_project(name, owner, repo) values('Spring Boot', 'spring-projects', 'spring-boot');
insert into git_hub_project(name, owner, repo) values('Spring LDAP', 'spring-projects', 'spring-ldap');
insert into git_hub_project(name, owner, repo) values('Spring REST Docs', 'spring-projects', 'spring-restdocs');
insert into git_hub_project(name, owner, repo) values('Spring Security', 'spring-projects', 'spring-security');
insert into git_hub_project(name, owner, repo) values('Spring Session', 'spring-projects', 'spring-session');

insert into jira_project(name, key) values('Spring AMQP', 'AMQP');
insert into jira_project(name, key) values('Spring Batch', 'BATCH');
insert into jira_project(name, key) values('Spring Framework', 'SPR');
insert into jira_project(name, key) values('Spring Integration', 'INT');
insert into jira_project(name, key) values('Spring Web Flow', 'SWF');
