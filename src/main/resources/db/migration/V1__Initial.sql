create table git_hub_project (
	id serial not null primary key,
	name character varying(255) not null,
	owner character varying(255) not null,
	repo character varying(255) not null
);

create table release (
	id serial not null primary key,
	project character varying(255) not null,
	name character varying(255) not null,
	date bigint not null
);

insert into git_hub_project(name, owner, repo) values('Spring Boot', 'spring-projects', 'spring-boot');
insert into git_hub_project(name, owner, repo) values('Spring REST Docs', 'spring-projects', 'spring-restdocs');
insert into git_hub_project(name, owner, repo) values('Spring Security', 'spring-projects', 'spring-security');
insert into git_hub_project(name, owner, repo) values('Spring Session', 'spring-projects', 'spring-session');
