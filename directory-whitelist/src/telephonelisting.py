import database
import re

class Person():
    '''Representation of an input person'''
    def __init__(self):
        self.id = -1
        self.first_name = None
        self.middle_name = None
        self.last_name = None
        self.telephones = [] # Telephone objects
    
    def __str__(self):
        teleph = '['
        teleph += ', '.join([t.__str__() for t in self.telephones])
        teleph += ']'
        middle_name = ''
        last_name = ''
        if self.middle_name:
            middle_name = ' ' + self.middle_name
        if self.last_name:
            last_name = self.last_name + ', '
        return "{0}{1}{2} - {3}".format(last_name, self.first_name, middle_name, teleph)
    

class Telephone():
    '''Representation of an input telephone number'''
    def __init__(self):
        self.id = -1
        self.person_id = -1
        self.type = None
        self.country = None
        self.area = None
        self.city_code = None
        self.local_number = None
        self.extension = None
    
    def __str__(self):
        output = ''
        if self.country:
            output += '+{0} '.format(self.country)
        if self.area:
            output += '({0}) '.format(self.area)
        if self.city_code:
            output += '{0}-'.format(self.city_code)
        if self.local_number:
            output += '{0}'.format(self.local_number)
        if self.extension:
            output += ' x{0}'.format(self.extension)
        return output
#        return "{0} - +{1} ({2}) {3}-{4}".format(self.type, self.country, self.area, self.city_code, self.local_number)

class Model:
    def save_person(self, person):
        '''Saves the person object into the database. Sets up the relationship between a person and a phone number as well'''
        db = database.open_db()
        cursor = db.cursor()
        # check if the person or number already exists
        person_by_name = self.find_person_by_name(person)
        person_by_number = self.find_person_by_phone(person.telephones[0])
        if person_by_name or person_by_number:
            raise ValueError('Cannot enter duplicate person or a duplicate phone number')
        try:
            cursor.execute(database.INSERT_PERSON, [person.first_name, person.middle_name, person.last_name])
            person_id = cursor.lastrowid
            t = person.telephones[0]
            t.person_id = person_id
            cursor.execute(database.INSERT_TELEPHONE,
                           [t.person_id, t.type, t.country, t.area, t.city_code, t.local_number, t.extension])
            db.commit()        
        except Exception as e:
            print(e)
            db.rollback()
            raise e
        else:
            cursor.close()
            
    def get_all(self):
        '''Selects all available person/telephone entries and returns them in an array of Person objects'''
        db = database.open_db()
        cursor = db.cursor()
        cursor.execute(database.SELECT_ALL)
        people = []
        for row in cursor:
            p = Person()
            p.id = int(row['p_id'])
            p.first_name = row['first_name']
            p.middle_name = row['middle_name']
            p.last_name = row['last_name']
            t = Telephone()
            t.id = int(row['t_id'])
            t.person_id = int(row['t_p_id'])
            t.type = row['type']
            t.country = row['country']
            t.area = row['area']
            t.city_code = row['city_code']
            t.local_number = row['local_number']
            t.extension = row['extension']
            p.telephones.append(t)
            people.append(p)
        db.close()
        return people
    
    def find_person_by_phone(self, phone):
        '''Finds a person based on their telephone number'''
        p = None
        db = database.open_db()
        cursor = db.cursor()
        # create prepared statement for use in execute()
        sql, values = self._create_prepared_statement(database.PERSON_BY_PHONE, phone)
        cursor.execute(sql, values)
        result = cursor.fetchone()
        if result:
            p = self._populate_person(result)
        db.close()
        return p
    
    def find_person_by_name(self, person):
        '''Finds a person based on their name'''
        p = None
        db = database.open_db()
        cursor = db.cursor()
        # create prepared statement for use in execute()
        sql, values = self._create_prepared_statement(database.PERSON_BY_NAME, person)
        cursor.execute(sql, values)
        result = cursor.fetchone()
        if result:
            p = self._populate_person(result)
        db.close()
        return p
    
    def delete_person(self, value):
        p = None
        if isinstance(value, Person):
            p = self.find_person_by_name(value)
            if not p:
                raise ValueError('{0} {1} not found in directory'.format(value.first_name, value.last_name))
        elif isinstance(value, Telephone):
            p = self.find_person_by_phone(value)
            if not p:
                raise ValueError('No person found for {0} in directory'.format(value.__str__()))
        else:
            raise ValueError('Must delete based on either a Person or a Telephone')
        db = database.open_db()
        cursor = db.cursor()
        
        try:
            cursor.execute(database.DELETE_PERSON, [p.id])
            t = p.telephones[0]
            cursor.execute(database.DELETE_TELEPHONE, [t.id, t.person_id])
            db.commit()
        except Exception as e:
            db.rollback()
            raise e
        else:
            cursor.close()
            db.close()
    
    def _create_prepared_statement(self, sql, value):
        ands = []
        values = []
        if isinstance(value, Telephone):
            if value.country:
                ands.append("t.country = ?")
                values.append(value.country)
            if value.area:
                ands.append("t.area = ?")
                values.append(value.area)
            if value.city_code:
                ands.append("t.city_code = ?")
                values.append(value.city_code)
            if value.local_number:
                ands.append("t.local_number = ?")
                values.append(value.local_number)
            if value.extension:
                ands.append("t.extension = ?")
                values.append(value.extension)
        elif isinstance(value, Person):
            if value.first_name:
                ands.append("p.first_name = ?")
                values.append(value.first_name)
            if value.last_name:
                ands.append("p.last_name = ?")
                values.append(value.last_name)
            if value.middle_name:
                ands.append("p.middle_name = ?")
                values.append(value.middle_name)
        sql += " AND ".join(ands)
        return sql, values
    
    def _populate_person(self, result):
        '''Populate a person and its associated telephone object based on the result dict'''
        p = Person()
        p.id = int(result['p_id'])
        p.first_name = result['first_name']
        p.middle_name = result['middle_name']
        p.last_name = result['last_name']
        t = Telephone()
        t.id = int(result['t_id'])
        t.person_id = int(result['t_p_id'])
        t.country = result['country']
        t.area = result['area']
        t.city_code = result['city_code']
        t.local_number = result['local_number']
        t.extension = result['extension']
        p.telephones.append(t)
        return p


def _prime_telephone_config(config):
    '''Helper function to initialize the config'''
    if 'area' not in config:
        config['area'] = None
    if 'city_code' not in config:
        config['city_code'] = None
    if 'country' not in config:
        config['country'] = None
    if 'extension' not in config:
        config['extension'] = None
    if 'local_number' not in config:
        config['local_number'] = None
    if 'person_id' not in config:
        config['person_id'] = None
    return config

def _create_telephone(config):
    telephone = Telephone()
    config = _prime_telephone_config(config)
    telephone.area = config['area']
    telephone.city_code = config['city_code']
    telephone.country = config['country']
    telephone.extension = config['extension']
    telephone.local_number = config['local_number']
    telephone.person_id = config['person_id']
    return telephone

def _create_person(config):
    person = Person()
    person.first_name = config['first_name']
    if 'middle_name' in config:
        person.middle_name = config['middle_name']
    if 'last_name' in config:
        person.last_name = config['last_name']
    return person

# rather than creating one huge regex, I've split the possible valid combinations
# into different regex associated them with telephone builder lambdas which build telephone
# object when they are successful, based off the groups() returned by the MatchObject
valid_telephones = [
    {'regex' : r'''
        1?\s?       # check for 1 and/or space
        \((\d{3})\) # three digits inside of parens - capture
        \s?
        (\d{3})     # three digits, no parens - capture
        -           # split by a dash - no capture
        (\d{4})     # four digits, no parens - capture
    ''',
    'builder' : lambda m: _create_telephone({'area':m[0], 'city_code':m[1], 'local_number':m[2]})},
    {'regex' : r'''
        \+?(\d{1,3})
        \s?
        1?\s?       # check for 1 and/or space
        \((\d{2,3})\) # three digits inside of parens - capture
        \s?
        (\d{3})     # three digits, no parens - capture
        -           # split by a dash - no capture
        (\d{4})     # four digits, no parens - capture
    ''',
    'builder' : lambda m: _create_telephone({'country':m[0],'area':m[1], 'city_code':m[2], 'local_number':m[3]})},
    {'regex' : r'''
        (\d{1,3})
        \s
        1?\s?       # check for 1 and/or space
        (\d{2,3})   # three digits inside of parens - capture
        \s
        (\d{3})     # three digits, no parens - capture
        \s           # split by a dash - no capture
        (\d{4})     # four digits, no parens - capture
    ''',
    'builder' : lambda m: _create_telephone({'country':m[0],'area':m[1], 'city_code':m[2], 'local_number':m[3]})},        
    {'regex' : r'''
        1?\s?       # check for 1 and/or space    
        \((\d{3})\) # same as above
        \s? 
        (\d{3})     
        -           
        (\d{4})     
        x           # 'x' (for extensions) - no capture
        (\d{5})     # 5 digits, no parens - capture
    ''',
    'builder' : lambda m: _create_telephone({'area':m[0], 'city_code':m[1], 'local_number':m[2], 'extension':m[3]})},
    {'regex' : r'''
        (\d{3})      # three digits, no parens - capture
        -            # split by a dash
        (\d{4})      # four digits, no parens - capture
    ''',
    'builder' : lambda m: _create_telephone({'city_code':m[0], 'local_number':m[1]})},
    {'regex' : r'''
        1?\s?       # check for 1 and/or space    
        \((\d{3})\) # same as above
        \s? 
        (\d{3})     
        -           
        (\d{4})     
        x           # 'x' (for extensions) - no capture
        (\d{5})     # 5 digits, no parens - capture
    ''',
    'builder' : lambda m: _create_telephone({'area':m[0],'city_code':m[1], 'local_number':m[2], 'extension':m[3]})}     
    
]

# Same concept as the telephones. Multiple regex that can parse out the content (if it's valid)
# and then an accompanying 'builder' lambda that returns a populated person based on the groups()
# of the MatchObject
valid_names = [
    {'regex' : r'''
        ([a-zA-Z]+)             # last name
        ,\s?                    # comma and an optional space between
        ([a-zA-Z]+)             # first name
        \s                      # space
        ([a-zA-Z]+|[a-zA-Z]\.)  # middle name of either one letter plus punctuation, or a full name
    ''',
    'builder' : lambda m: _create_person({'last_name':m[0], 'first_name':m[1], 'middle_name':m[2]})},
    {'regex' : r'''
        ([oO]'[a-zA-Z]+)        # last name plus beginning O'
        ,\s?                    # comma and an optional space between
        ([a-zA-Z]+)             # first name
        \s                      # space
        ([a-zA-Z]+|[a-zA-Z]\.)  # middle name of either one letter plus punctuation, or a full name
    ''',
    'builder' : lambda m: _create_person({'last_name':m[0], 'first_name':m[1], 'middle_name':m[2]})},
    {'regex' : r'''
        ([a-zA-Z]+)             # first name
        \s                      # comma and an optional space between
        ([a-zA-Z]+|[a-zA-Z]\.)  # middle name of either one letter plus punctuation, or a full name
        \s
        ([oO]'[a-zA-Z]+)        # last name
    ''',
    'builder' : lambda m: _create_person({'first_name':m[0], 'middle_name':m[1], 'last_name':m[2]})},
    {'regex' : r'''
        ([a-zA-Z]+)             # first name
        \s                      # comma and an optional space between
        ([a-zA-Z]+|[a-zA-Z]\.)  # middle name of either one letter plus punctuation, or a full name
        \s
        ([a-zA-Z]+)        # last name
    ''',
    'builder' : lambda m: _create_person({'first_name':m[0], 'middle_name':m[1], 'last_name':m[2]})},                    
    {'regex' : r'''
        ([a-zA-Z]+)   # simple, first name
        \s            # no comma, requires a space in between
        ([a-zA-Z]+)   # simple, last name
    ''',
    'builder' : lambda m: _create_person({'first_name':m[0], 'last_name':m[1]})},
    {'regex' : r'''
        ([a-zA-Z]+)   # last name
        ,\s?          # comma and an optional space between
        ([a-zA-Z]+)   # first name
    ''',
    'builder' : lambda m: _create_person({'last_name':m[0], 'first_name':m[1]})},
    {'regex' : r'''
        ([a-zA-Z]+)        # simple, first name
        \s                 # no comma, requires a space in between
        ([oO]'[a-zA-Z]+)   # simple, last name plus beginning O'
    ''',
    'builder' : lambda m: _create_person({'first_name':m[0], 'last_name':m[1]})},
    {'regex' : r'''
        ([oO]'[a-zA-Z]+)   # last name plus beginning O'
        ,\s?               # comma and an optional space between
        ([a-zA-Z]+)        # first name
    ''',
    'builder' : lambda m: _create_person({'last_name':m[0], 'first_name':m[1]})},        
    {'regex' : r'''
        ([a-zA-Z]+)   # first name
    ''',
    'builder' : lambda m: _create_person({'first_name':m[0]})}        
]

class Parser:    
    def parse_add(self, input):
        telephone = None
        person = None
        for entry in valid_names:
            # call match, because it needs to be the beginning of the string
            match = re.match(entry['regex'], input, re.X)
            if match:
                person = entry['builder'](match.groups())
                break
        if not person:
            return None
        # remove the name from the input string, and match the telephone on that
        input = input[match.end() + 1:]
        print(input)
        for entry in valid_telephones:
            # call search, because it's after the name
            match = re.search('^' + entry['regex'] + '$', input, re.X)
            if match:
                telephone = entry['builder'](match.groups())
                break
        if not telephone:
            return None
        person.telephones.append(telephone)
        return person
    
    def parse_delete(self, input):
        '''Parses the delete input syntax into a person or a telephone object'''
        # the setup regex are meant to match input at any point
        # these loops need to match input as a single unit, seeing them as starting and ending
        # with the regex - that's why '^' is added to the beginning (starts with) and '$' is added
        # to the ending (ends with)
        for entry in valid_telephones:
            match = re.match('^' + entry['regex'] + '$', input, re.X)
            if match:
                telephone = entry['builder'](match.groups())
                return telephone
        for entry in valid_names:
            match = re.match('^' + entry['regex'] + '$', input, re.X)
            if match:
                person = entry['builder'](match.groups())
                return person
        return None
    
    def suggest_add(self, input):
        return "Try again" # go through bad inputs and make suggestions
    
    def suggest_delete(self, input):
        return "Try again" # go through bad inputs and make suggestions

