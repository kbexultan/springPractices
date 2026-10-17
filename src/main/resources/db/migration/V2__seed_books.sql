INSERT INTO books (title, author, isbn, publication_year, available)
VALUES ('Абай жолы', 'Мұхтар Әуезов', '9786019000001', 1942, true)
    ON CONFLICT (isbn) DO NOTHING;