'''
Created on Apr 24, 2010

Database functionality for the phone directory
@author: johnpcamara
'''

CREATE_PERSON = '''
    CREATE TABLE IF NOT EXISTS person (
        id integer,
        first_name text,
        middle_name text,
        last_name text,
        PRIMARY KEY(id ASC)
    )
'''

CREATE_TELEPHONE = '''
    CREATE TABLE IF NOT EXISTS telephone (
        id integer,
        person_id integer,
        type text,
        country text,
        area text,
        city_code text,
        local_number text,
        extension text,
        PRIMARY KEY(id ASC)
    )
'''

INSERT_TELEPHONE = '''
    INSERT INTO telephone (id, person_id, type, country, area, city_code, local_number, extension) VALUES (NULL, ?, ?, ?, ?, ?, ?, ?)
'''

INSERT_PERSON = '''
    INSERT INTO person (id, first_name, middle_name, last_name) VALUES (NULL, ?, ?, ?)
'''

DELETE_TELEPHONE = '''
    DELETE FROM telephone WHERE id = ? AND person_id = ? 
'''

DELETE_PERSON = '''
    DELETE FROM person WHERE id = ?
'''

SELECT_ALL = '''
    SELECT p.id as p_id, p.first_name, p.middle_name, p.last_name, t.id as t_id, 
            t.person_id as t_p_id, t.type, t.country, t.area, t.city_code, t.local_number, t.extension
    FROM person p INNER JOIN telephone t ON p.id = t.person_id
'''
# you must have at least a first name
PERSON_BY_NAME = '''
    SELECT p.id as p_id, p.first_name, p.middle_name, p.last_name, t.id as t_id, 
            t.person_id as t_p_id, t.type, t.country, t.area, t.city_code, t.local_number, t.extension
    FROM person p INNER JOIN telephone t ON p.id = t.person_id
    WHERE 
'''
# telephone number is required to have a city code and local number
PERSON_BY_PHONE = ''' 
    SELECT p.id as p_id, p.first_name, p.middle_name, p.last_name, t.id as t_id, 
            t.person_id as t_p_id, t.type, t.country, t.area, t.city_code, t.local_number, t.extension
    FROM person p INNER JOIN telephone t ON p.id = t.person_id
    WHERE 
'''

import sqlite3
    
def open_db():
    '''Open the database connection and return it'''
    create_db()
    db = sqlite3.connect('listing.sqlite')
    db.row_factory = sqlite3.Row
    return db
    
def create_db():
    # open the db
    db = sqlite3.connect('listing.sqlite')
    # create the tables, if they don't already exist
    cursor = db.cursor()
    cursor.execute(CREATE_PERSON)
    cursor.execute(CREATE_TELEPHONE)
    db.commit()
    # close 'er out
    cursor.close()
    db.close()