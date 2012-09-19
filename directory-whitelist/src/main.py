import telephonelisting
import re
import listingtests

valid_commands = [r'ADD\s', r'DEL\s', 'HELP', 'LIST', 'QUIT']
valid_command_regex = re.compile("^(" + r"|".join(valid_commands) + ")") # starts with one of the commands ^(CMD1|CMD2|CMDN...)
example_data = listingtests.good_values

class ListingController:
    def __init__(self):
        self.view = UI(self)
        self.parser = telephonelisting.Parser()
        self.model = telephonelisting.Model()
    
    def start(self):
        '''starts up the app'''
        self.view.start()
        
    def add(self, input):
        '''add the input person/telephone to the database'''
        person = self.parser.parse_add(input)
        if not person:
            raise ValueError('Invalid Add command: ' + self.parser.suggest_add(input))
        self.model.save_person(person)
    
    def delete(self, input):
        '''delete the person from the database'''
        obj = self.parser.parse_delete(input)
        if not obj:
            raise ValueError('Invalid Delete command: ' + self.parser.suggest_delete(input))
        self.model.delete_person(obj)
        
    def list(self, input):
        '''list all entries in the database'''
        people = self.model.get_all()
        output = ''
        for p in people:
            output += p.__str__()
            output += '\n'
        if output == '':
            output = 'No Entries Found'
        return output

class UI:
    def help(self, type = ""):
        '''Help listing for convenient reuse'''
        help_info = '''
            Please choose one of the following operations to continue your interaction with the system
            ADD  - <Name> <Telephone#>
            DEL  - <Name>
            DEL  - <Telephone#>
            LIST - Produce a list of members of the database
            HELP - Show these options again
            QUIT - Quits the application
        '''
        return help_info
        
    def __init__(self, controller):
        self.controller = controller
        
    def start(self):
        '''Introduces the application to the user and then accepts user input'''
        print('Welcome to the JP Camara\'s Telephone Directory service!')
        print(self.help())
        self.input()

    def input(self):
        '''Accepts input from the user and hands it off to be parsed'''
        user_input = input('> ')
        self.parse_input(user_input)
        # start accepting input again
        self.input()        
        
    def parse_input(self, input):
        '''Checks the command, then hands data off to be validated, processed and inserted/deleted from the database'''
        try:
            normalized = self.normalize_input(input)
            match = valid_command_regex.match(normalized)
            if not match:
                print('Command "' + normalized + '" was in an invalid format')
                print(self.help())
                return
            command = match.groups()[0]
            self.execute_command(command, normalized.replace(command, ''))
        except Exception as e:
            print(e)
            print(self.help())
            
    def normalize_input(self, input):
        if not input:
            raise ValueError('Input is invalid')
        return ' '.join(input.split())
            
    def execute_command(self, command, input):
        command_stripped = command.strip()
        if command_stripped == 'ADD':
            self.controller.add(input)
            print('Add successful')
        elif command_stripped == 'DEL':
            self.controller.delete(input)
            print('Delete successful')
        elif command_stripped == 'LIST':
            print(self.controller.list(input))
        elif command_stripped == 'HELP':
            print(self.help())
            print('Here is some valid example data')
            output = []
            length = len(example_data)
            for e in example_data:
                if len(output) > 5 or len(output) > length:
                    print(output)
                    output = []
                output.append(e)
                length -= 1
        elif command_stripped == 'QUIT':
            import sys
            print('Goodbye!')
            sys.exit()

if __name__ == "__main__":
    controller = ListingController()
    controller.start()
    
