# To change this template, choose Tools | Templates
# and open the template in the editor.

import unittest
import telephonelisting


good_values = ('1(703)111-2491',
               '1 (703)111-5121x12345',
               '(703) 111-2121x12345',
               '(703) 111-2134',
               '555-5555',
               '123-1234',
               '+1(703)111-2121',
               '+32 (21) 212-2324',
               '1(703)111-2394',
               '011 701 111 1234',
               '011 1 703 111 1234',
               'Bruce Schneier',
               'Schneier, Bruce',
               'Schneier,Bruce',
               'John Paul Camara',
               'Schneier, Bruce Wayne',
               "O'Malley, John F.",
               "O'Malley, John",
               'Cher')

other_good_values = ('Bruce Schneier (703)111-2121',
                   'Bruce Schneier 1 (703)111-2121x12345',
                   'Bruce Schneier 123-1234',
                   'Schneier, Bruce Wayne (309)111-2039',
                   'John Paul Camara +12(203)309-2039')

bad_values = ('123',
                  '1/703/123/1234',
                  'Nr 102-123-1234',
                  '<script>alert("XSS")</script>',
                  '7031111234',
                  '+1234 (201) 123-1234',
                  '(703) 123-1234 ext 204'
                 )

class  TelephoneListingTestCase(unittest.TestCase):
    '''Test that the parsing is working properly'''
    def test_good_values_create_telephone_or_person(self):
        for value in good_values:
            print(telephonelisting.Parser().parse_delete(value))
            
    def test_good_values_for_add(self):
        for value in other_good_values:
            print(telephonelisting.Parser().parse_add(value))

class BadTelephoneListingTestCase(unittest.TestCase):
    '''tests invalid inputs for the telephone'''
    def test_bad_values_for_telephone(self):
        print('bad values!!')
        for value in bad_values:
            print(telephonelisting.Parser().parse_delete(value))
        print('done with bad values!!')
        print()

if __name__ == '__main__':
    unittest.main()

