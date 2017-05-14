# family-tree

This utility was originally requested by relatives to produce lists of family members in specific formats. Having an extended family of over two hundred members in five generations gives me a complete set of use cases.

The utility reads a list of family members from a text file to generate a family tree of various styles in HTML format.

The records in the data file are arranged as one line per person in the following tab delimited fields in the following order:
 1. ID of person
 2. name
 3. alternative name, often the name used in the country of residence
 4. gender (F/M)
 5. date of birth (yyyymmdd)
 6. date of birth in traditional Chinese calendar if cannot be calculated
 7. date of death (yyyymmdd, 0 if still alive)
 8. date of death in traditional Chinese calendar if cannot be calculated
 9. ID of father or 0 if not included (for the ancestor or a spouse)
10. ID of mother or 0 if not included (for the ancestor or a spouse)
11. ID of current spouse if married, negative values indicate divorce

Output files include lists organized by birthday in grid of month and date, list sorted by age, list sorted by generation then by age, the traditional family tree, and an interactive list with links.
